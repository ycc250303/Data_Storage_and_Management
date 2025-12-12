import asyncio
import aiohttp
import random
import time
import ssl
import os
from fake_useragent import UserAgent

MAX_RETRIES = 3  # 最大重试次数

# 使用 fake_useragent
ua = UserAgent()

# 自定义浏览器版本
BROWSER_VERSIONS = [
    ("96.0.4664.110", "Windows NT 10.0; Win64; x64"),
    ("98.0.4758.102", "Windows NT 10.0; Win64; x64"),
    ("95.0.4638.69", "Macintosh; Intel Mac OS X 10_15_7"),
]

ACCEPT_LANGUAGES = [
    "en-US,en;q=0.9",
    "en-GB,en;q=0.9,en-US;q=0.8",
]

ACCEPT_ENCODINGS = [
    "gzip, deflate, br",
    "gzip, deflate",
]


def get_random_headers():
    browser_version, os_info = random.choice(BROWSER_VERSIONS)

    user_agent = (
        f"Mozilla/5.0 ({os_info}) AppleWebKit/537.36 "
        f"(KHTML, like Gecko) Chrome/{browser_version} Safari/537.36"
    )

    return {
        "user-agent": user_agent,
        "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "accept-language": random.choice(ACCEPT_LANGUAGES),
        "accept-encoding": random.choice(ACCEPT_ENCODINGS),
        "cache-control": random.choice(["max-age=0", "no-cache"]),
        "referer": "https://www.amazon.com/",
    }


async def update_cookie():
    """生成真实 Cookie"""
    session_id = f"{random.randint(100000000, 999999999)}-{random.randint(100000000, 999999999)}"
    session_token = "".join(random.choice("abcdefghijklmnopqrstuvwxyz0123456789") for _ in range(80))
    ubid_main = f"{random.randint(100, 999)}-{random.randint(1000000, 9999999)}"

    return (
        f"session-id={session_id}; "
        f"session-token={session_token}; "
        f"ubid-main={ubid_main}"
    )


async def fetch_single_page(url, asin):
    """核心：获取单个页面的 HTML 内容"""
    ssl_context = ssl.create_default_context()

    async with aiohttp.ClientSession(
        connector=aiohttp.TCPConnector(limit=50, ssl=ssl_context)
    ) as session:

        for retry in range(MAX_RETRIES):
            try:
                headers = get_random_headers()
                headers["cookie"] = await update_cookie()

                async with session.get(url, headers=headers, timeout=20) as resp:
                    content = await resp.text()

                    # 基本反爬检测
                    if "captcha" in content.lower() or len(content) < 3000:
                        print(f"[警告] 反爬触发，重试第{retry + 1}次...")
                        await asyncio.sleep(random.uniform(3, 8))
                        continue

                    # 🚀 成功后直接保存到 ../results
                    save_dir = "../results"
                    os.makedirs(save_dir, exist_ok=True)
                    
                    file_path = f"{save_dir}/{asin}.html"
                    with open(file_path, "w", encoding="utf-8") as f:
                        f.write(content)

                    print(f"请求成功！文件已保存到：{file_path}")
                    return file_path

            except Exception as e:
                print(f"[错误] {e}, 正在重试第{retry + 1}次...")
                await asyncio.sleep(random.uniform(3, 8))

        print("达到最大重试次数，失败。")
        return None


async def main():
    asin = "B004VZW92W"
    url = f"https://www.amazon.com/dp/{asin}"

    await fetch_single_page(url, asin)


if __name__ == "__main__":
    asyncio.run(main())
