import asyncio
import aiohttp
import random
import ssl
import os

# ================== 基础配置 ==================
MAX_RETRIES = 3
PROXY = "http://127.0.0.1:7897"  # Clash 默认端口
ASIN_FILE = "../unique_product_ids.txt"  # ASIN 列表文件
RESULT_DIR = "../results"
FAILED_DIR = "../failed"
START_LINE = 190000  # 从第几行开始抓取（0 表示第一行）

MAX_CONCURRENT = 50   # 并发数量，不建议超过 5
BATCH_SIZE = 500      # 每批抓取数量
BATCH_SLEEP = (18, 30)  # 每批后休眠

# ================== 浏览器 Cookie（必须真实） ==================
AMAZON_COOKIE = (
    "session-id=145-3226986-2591160; "
    "session-token=//3a1z8kWavli4dC0+mLhzSzhgp+8iOEu/pLNU/HJAjQx+7JiqMfbuTYR+hpY5GOw/FCTClaakVHP8+oNNlgbp4gyxwF2Mw3uVVfCTcTtnSocZEwDVIM5zKfHbdg9GrG5KGcb1098nCaKFQ615Zczmzt+fajAOzKWF5z4WwL8pCtARiO7MIEroh35zbNB1Rft2mW3GwlMXd8v+V76T4aByDtjABtyZmqLBcFdqCvyJPm2MjDWIE//LbkEMNAV2VD2T8VORGfjH1Q6S4yMVq6JT1iwOcN+vCgOSlM5g4n0w0RNnlPSVmwUinvcHWYLNvjmpOWO/UPpPM5hw6mfqxO8hq7CpLqGBCm4k; "
    "ubid-main=135-8397825-3395814; "
    "lc-main=en_US; "
    "i18n-prefs=USD"
)

# ================== Prime 专用 Headers ==================
HEADERS = {
    "user-agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/120.0.0.0 Safari/537.36"
    ),
    "accept": (
        "text/html,application/xhtml+xml,application/xml;q=0.9,"
        "image/avif,image/webp,*/*;q=0.8"
    ),
    "accept-language": "en-US,en;q=0.9",
    "referer": "https://www.amazon.com/gp/video/storefront",
    "upgrade-insecure-requests": "1",
    "cookie": AMAZON_COOKIE,
}

# ================== 核心函数 ==================
async def fetch_single_prime_page(semaphore, session, asin: str):
    async with semaphore:
        # 跳过已抓取的文件
        result_path = os.path.join(RESULT_DIR, f"{asin}.html")
        failed_path = os.path.join(FAILED_DIR, f"{asin}.html")
        url = f"https://www.amazon.com/gp/video/detail/{asin}"

        for retry in range(1, MAX_RETRIES + 1):
            try:
                async with session.get(
                    url,
                    headers=HEADERS,
                    proxy=PROXY,
                    timeout=30
                ) as resp:

                    html = await resp.text()

                    # ===== 反爬检测 =====
                    if (
                        "captcha" in html.lower()
                        or "The availability of Prime Video and its content differs by country or region" in html
                        or "robot check" in html.lower()
                    ):
                        print(f"[WARN] {asin} 页面异常，第 {retry} 次")
                        await asyncio.sleep(random.uniform(20, 30))
                        continue

                    # ===== 保存成功页面 =====
                    os.makedirs(RESULT_DIR, exist_ok=True)
                    with open(result_path, "w", encoding="utf-8") as f:
                        f.write(html)

                    print(f"✅ {asin} 保存成功")
                    # 每个请求后随机延时，降低被封风险
                    await asyncio.sleep(random.uniform(1, 2))
                    return

            except Exception as e:
                print(f"[ERROR] {asin} {e}，第 {retry} 次重试")
                await asyncio.sleep(random.uniform(20, 30))

        # 达到最大重试，保存异常页面到 failed
        os.makedirs(FAILED_DIR, exist_ok=True)
        with open(failed_path, "w", encoding="utf-8") as f:
            f.write(f"[FAILED] {asin} 无法抓取\n")
        print(f"❌ {asin} 达到最大重试次数，保存到 failed")

# ================== 批量抓取 ==================
async def fetch_multiple_prime_pages(start_line: int = 0):
    ssl_context = ssl.create_default_context()
    semaphore = asyncio.Semaphore(MAX_CONCURRENT)

    # 读取 ASIN 列表
    with open(ASIN_FILE, "r", encoding="utf-8") as f:
        asins = [line.strip() for line in f.readlines()]

    asins = asins[start_line:]

    async with aiohttp.ClientSession(connector=aiohttp.TCPConnector(ssl=ssl_context)) as session:
        for i in range(0, len(asins), BATCH_SIZE):
            batch = asins[i:i + BATCH_SIZE]

            print(f"\n🚀 开始批次 {start_line + i} ~ {start_line + i + len(batch) - 1}")

            tasks = [
                fetch_single_prime_page(semaphore, session, asin)
                for asin in batch
            ]

            await asyncio.gather(*tasks)

            print("🛑 批次完成，进入冷却")
            await asyncio.sleep(random.uniform(*BATCH_SLEEP))

# ================== 测试入口 ==================
if __name__ == "__main__":
    asyncio.run(fetch_multiple_prime_pages(START_LINE))
