import asyncio
import aiohttp
import random
import ssl
import os
import time
from datetime import datetime

# ========== 基础配置 ==========
START_POSITION = 181000
REQUEST_INTERVAL = (10, 15)   # Prime 推荐 8~15 秒
MAX_RETRIES = 3

ASIN_FILE = "../unique_product_ids.txt"
RESULT_DIR = "../results"
FAILED_FILE = "../failed_prime.txt"

# ========== 真实 Cookie（浏览器导出，固定）==========
AMAZON_COOKIE = (
    "session-id=xxx; "
    "session-token=xxx; "
    "ubid-main=xxx; "
    "lc-main=en_US; "
    "i18n-prefs=USD;"
)

# ========== 固定真实 UA ==========
HEADERS = {
    "user-agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/120.0.0.0 Safari/537.36"
    ),
    "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "accept-language": "en-US,en;q=0.9",
    "referer": "https://www.amazon.com/",
    "cookie": AMAZON_COOKIE
}


async def fetch_prime_page(session, asin):
    url = f"https://www.amazon.com/gp/video/detail/{asin}"

    for retry in range(MAX_RETRIES):
        try:
            async with session.get(url, headers=HEADERS, timeout=30) as resp:
                html = await resp.text()

                if (
                    "captcha" in html.lower()
                    or "无法为您当前所在地区提供服务" in html
                    or len(html) < 6000
                ):
                    print(f"[WARN] {asin} 页面异常，第 {retry+1} 次重试")
                    await asyncio.sleep(random.uniform(5, 10))
                    continue

                path = os.path.join(RESULT_DIR, f"{asin}.html")
                with open(path, "w", encoding="utf-8") as f:
                    f.write(html)

                print(f"[OK] {asin} 保存成功")
                return True

        except Exception as e:
            print(f"[ERROR] {asin} {e}")
            await asyncio.sleep(random.uniform(20, 30))

    with open(FAILED_FILE, "a") as f:
        f.write(f"{asin}\n")
    return False


async def main():
    os.makedirs(RESULT_DIR, exist_ok=True)

    with open(ASIN_FILE, "r") as f:
        asins = [line.strip() for line in f.readlines()[START_POSITION:]]

    ssl_context = ssl.create_default_context()

    async with aiohttp.ClientSession(
        connector=aiohttp.TCPConnector(ssl=ssl_context)
    ) as session:

        for asin in asins:
            await fetch_prime_page(session, asin)
            await asyncio.sleep(random.uniform(*REQUEST_INTERVAL))


if __name__ == "__main__":
    asyncio.run(main())
