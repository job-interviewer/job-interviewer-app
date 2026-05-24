import discord
import os
import requests
from discord.ext import commands
from pathlib import Path

def _load_env():
    """
    Load environment variables from a `.env` file located in the same directory as this script.
    
    Reads the file using UTF-8, ignores empty lines and lines starting with `#`, parses lines of the form `KEY=VALUE`, strips surrounding whitespace and surrounding double quotes from values, and sets each key in the process environment only if it is not already defined.
    """
    env_path = Path(__file__).parent / ".env"
    if env_path.exists():
        for line in env_path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                key, _, val = line.partition("=")
                os.environ.setdefault(key.strip(), val.strip().strip('"'))

_load_env()

BOT_TOKEN = os.environ["DISCORD_BOT_KEY"]
CHANNEL_ID = 1489595201398902946
BASE_URL = "http://localhost:8080"

sessions = {}

intents = discord.Intents.default()
intents.message_content = True
bot = commands.Bot(command_prefix="!", intents=intents, help_command=None)


def api_start_interview(resume_text: str, follow_up: bool = True):
    """
    Start an interview session on the backend using the provided resume text.
    
    Parameters:
        resume_text (str): Resume or cover letter text to be submitted to the backend.
        follow_up (bool): Whether the backend should enable follow-up questions for the session.
    
    Returns:
        tuple: (response_json, status_code)
            response_json (dict): Parsed JSON body returned by the backend.
            status_code (int): HTTP status code from the backend response.
    """
    resp = requests.post(
        f"{BASE_URL}/api/interview/start",
        json={"coverLetterText": resume_text, "followUpEnabled": follow_up},
        timeout=90
    )
    return resp.json(), resp.status_code


def api_submit_answer(session_id: str, question_id: str, answer: str):
    """
    Submit an answer for a question in an existing interview session to the backend.
    
    Parameters:
        session_id (str): The interview session identifier returned by the backend.
        question_id (str): The backend's identifier for the question being answered.
        answer (str): The user's answer text to submit.
    
    Returns:
        tuple: A pair (response_json, status_code) where `response_json` is the parsed JSON body from the backend and `status_code` is the HTTP status code.
    """
    resp = requests.post(
        f"{BASE_URL}/api/interview/{session_id}/answer",
        json={"questionId": question_id, "answer": answer},
        timeout=90
    )
    return resp.json(), resp.status_code


def api_complete(session_id: str):
    """
    Mark the interview session with the given session_id as complete on the backend and return the backend's response.
    
    Parameters:
        session_id (str): The interview session identifier to complete.
    
    Returns:
        tuple: A pair (response_json, status_code) where `response_json` is the parsed JSON body from the backend and `status_code` is the HTTP status code.
    """
    resp = requests.post(
        f"{BASE_URL}/api/interview/{session_id}/complete",
        timeout=10
    )
    return resp.json(), resp.status_code


def format_questions(questions: list) -> str:
    """
    Format a list of question objects into a human-readable string for display.
    
    Parameters:
        questions (list): Sequence of mappings each containing at least `orderIndex` (display number) and `content` (question text).
    
    Returns:
        str: A string where each question is rendered as "**Q{orderIndex}.** {content}" with a blank line between questions.
    """
    lines = []
    for q in questions:
        lines.append(f"**Q{q['orderIndex']}.** {q['content']}")
    return "\n\n".join(lines)


@bot.event
async def on_ready():
    """
    Handle the Discord bot's ready event by printing the logged-in bot user and the configured channel ID to stdout.
    """
    print(f"✅ 봇 로그인: {bot.user}")
    print(f"📌 채널 ID: {CHANNEL_ID}")


@bot.event
async def on_message(message: discord.Message):
    """
    Handle incoming Discord messages: process bot commands, start/drive interview sessions, and route answers to the backend.
    
    This function ignores messages from bots and messages not in the configured channel, forwards commands to the command processor, and implements the chat flow for three user-facing actions:
    - `!면접 [resume]`: validates the resume text, calls the backend to start an interview session, stores session state in the module-level `sessions` mapping, and sends the first question.
    - `!종료`: ends the user's active session by calling the backend complete endpoint, removes session state, and sends a completion summary.
    - `!도움말`: sends usage instructions.
    
    While a session is active and awaiting an answer, any non-command message from the session user is treated as an answer: the function submits the answer to the backend, handles follow-up questions (presenting them when returned), advances to the next question, and completes the session when all questions are answered. All user-visible outcomes are delivered via channel messages; network errors and non-200 backend responses are reported to the user.
    
    Parameters:
        message (discord.Message): The incoming Discord message to handle.
    
    """
    if message.author.bot:
        return
    if message.channel.id != CHANNEL_ID:
        return

    await bot.process_commands(message)

    user_id = message.author.id

    # === !면접 명령어 ===
    if message.content.startswith("!면접"):
        resume = message.content[4:].strip()
        if not resume:
            await message.channel.send(
                "📝 **사용법:** `!면접 [이력서 내용을 여기에 붙여넣기]`\n\n"
                "예시:\n```\n!면접 저는 3년차 백엔드 개발자입니다. Spring Boot와 Kotlin을 주로 사용하며...\n```"
            )
            return

        if len(resume) < 10:
            await message.channel.send("⚠️ 이력서 내용이 너무 짧습니다. 10자 이상 입력해주세요.")
            return

        loading_msg = await message.channel.send(
            f"⏳ **{message.author.display_name}**님의 이력서를 분석 중입니다...\n"
            "> Gemini AI가 맞춤형 면접 질문을 생성하고 있습니다. 잠시 기다려주세요."
        )

        try:
            data, status = api_start_interview(resume)
        except Exception as e:
            await loading_msg.edit(content=f"❌ 서버 연결 실패: {e}")
            return

        if status != 200:
            await loading_msg.edit(content=f"❌ 면접 시작 실패: `{data.get('message', status)}`")
            return

        session_id = data["sessionId"]
        job_field = data["jobField"]
        questions = data["questions"]

        sessions[user_id] = {
            "session_id": session_id,
            "job_field": job_field,
            "questions": questions,
            "current_q_index": 0,
            "current_question_id": questions[0]["questionId"],
            "follow_up_enabled": True,
            "waiting_for_answer": True,
            "is_follow_up": False,
        }

        q = questions[0]
        await loading_msg.delete()
        await message.channel.send(
            f"━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
            f"🎙️ **AI 면접 시작** — {message.author.mention}\n"
            f"📋 **감지된 직무:** {job_field}\n"
            f"❓ **총 질문 수:** {len(questions)}개 (꼬리질문 제외)\n"
            f"━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n"
            f"**[1/{len(questions)}] 첫 번째 질문:**\n\n"
            f"💬 {q['content']}\n\n"
            f"*답변을 채팅창에 입력해주세요. `!종료`를 입력하면 면접을 마칩니다.*"
        )
        return

    if message.content.strip() == "!종료":
        if user_id not in sessions:
            await message.channel.send("⚠️ 진행 중인 면접 세션이 없습니다. `!면접 [이력서]`로 시작하세요.")
            return

        sess = sessions[user_id]
        try:
            data, status = api_complete(sess["session_id"])
        except Exception as e:
            await message.channel.send(f"❌ 면접 종료 실패: {e}")
            return

        del sessions[user_id]
        await message.channel.send(
            f"✅ **면접 완료!** — {message.author.mention}\n"
            f"📋 직무: {sess['job_field']}\n"
            f"🕐 완료 시각: `{data.get('completedAt', '확인 불가')}`\n\n"
            f"수고하셨습니다! 다시 연습하려면 `!면접 [이력서]`를 입력하세요. 💪"
        )
        return

    if message.content.strip() == "!도움말":
        await message.channel.send(
            "**📖 AI 면접 봇 사용법**\n\n"
            "• `!면접 [이력서내용]` — 이력서를 붙여넣고 면접 시작\n"
            "• `[답변 입력]` — 면접 중 답변 제출\n"
            "• `!종료` — 현재 면접 종료\n"
            "• `!도움말` — 이 메시지\n\n"
            "*꼬리질문이 있으면 추가 질문이 나타납니다. 꼬리질문 포함 최대 ~10문항*"
        )
        return

    if user_id not in sessions:
        return

    sess = sessions[user_id]
    if not sess.get("waiting_for_answer"):
        return

    answer = message.content.strip()
    if not answer:
        return

    sess["waiting_for_answer"] = False
    questions = sess["questions"]
    current_idx = sess["current_q_index"]
    total = len(questions)

    thinking_msg = await message.channel.send("🤔 답변을 평가 중입니다...")

    try:
        data, status = api_submit_answer(sess["session_id"], sess["current_question_id"], answer)
    except Exception as e:
        sess["waiting_for_answer"] = True
        await thinking_msg.edit(content=f"❌ 서버 오류: {e}")
        return

    await thinking_msg.delete()

    if status != 200:
        sess["waiting_for_answer"] = True
        await message.channel.send(f"❌ 오류: `{data.get('message', status)}`")
        return

    needs_follow_up = data.get("needsFollowUp", False)
    follow_up_q = data.get("followUpQuestion")

    if needs_follow_up and follow_up_q:
        sess["current_question_id"] = follow_up_q["questionId"]
        sess["is_follow_up"] = True
        sess["waiting_for_answer"] = True

        await message.channel.send(
            f"🔍 **꼬리질문** — [{current_idx + 1}/{total}]\n\n"
            f"💬 {follow_up_q['content']}\n\n"
            f"*답변을 입력해주세요.*"
        )
    else:
        next_idx = current_idx + 1
        if next_idx >= total:
            try:
                complete_data, _ = api_complete(sess["session_id"])
            except Exception:
                complete_data = {}

            del sessions[user_id]
            await message.channel.send(
                f"🎉 **모든 질문 완료!** — {message.author.mention}\n\n"
                f"📋 직무: {sess['job_field']}\n"
                f"✅ {total}개 질문 모두 답변 완료\n"
                f"🕐 완료: `{complete_data.get('completedAt', '확인 불가')}`\n\n"
                f"면접 연습 수고하셨습니다! 💪\n"
                f"다시 연습하려면 `!면접 [이력서]`를 입력하세요."
            )
        else:
            sess["current_q_index"] = next_idx
            next_q = questions[next_idx]
            sess["current_question_id"] = next_q["questionId"]
            sess["is_follow_up"] = False
            sess["waiting_for_answer"] = True

            await message.channel.send(
                f"**[{next_idx + 1}/{total}] 다음 질문:**\n\n"
                f"💬 {next_q['content']}\n\n"
                f"*답변을 입력해주세요.*"
            )


print("Discord bot starting...")
bot.run(BOT_TOKEN)
