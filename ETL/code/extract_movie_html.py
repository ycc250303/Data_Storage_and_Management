import asyncio
import aiohttp
import random
import time
import os
import ssl
import json
from fake_useragent import UserAgent
from datetime import datetime, timedelta

START_POSITION = 170000  # 开始位置
MAX_WORKERS = 20  # 同时运行的线程数
BATCH_SIZE = 50  # 每批处理的ID数量
MAX_RETRIES = 3  # 最大重试次数

# 使用fake_useragent生成随机User-Agent
ua = UserAgent()

# 预定义一些常见的浏览器版本和操作系统组合
BROWSER_VERSIONS = [
    ("90.0.4430.212", "Windows NT 10.0; Win64; x64"),
    ("91.0.4472.124", "Windows NT 10.0; Win64; x64"),
    ("92.0.4515.107", "Windows NT 10.0; Win64; x64"),
    ("93.0.4577.82", "Windows NT 10.0; Win64; x64"),
    ("94.0.4606.81", "Macintosh; Intel Mac OS X 10_15_7"),
    ("95.0.4638.69", "Macintosh; Intel Mac OS X 10_15_7"),
    ("96.0.4664.110", "Windows NT 10.0; Win64; x64"),
    ("97.0.4692.71", "Macintosh; Intel Mac OS X 10_15_7"),
    ("98.0.4758.102", "Windows NT 10.0; Win64; x64"),
    ("99.0.4844.82", "Macintosh; Intel Mac OS X 10_15_7"),
]

# 预定义一些常见的Accept-Language
ACCEPT_LANGUAGES = [
    "en-US,en;q=0.9",
    "en-US,en;q=0.8",
    "en-GB,en;q=0.9,en-US;q=0.8",
    "en-US,en-GB;q=0.9,en;q=0.8",
    "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7",
]

# 预定义一些常见的Accept-Encoding
ACCEPT_ENCODINGS = [
    "gzip, deflate, br",
    "gzip, deflate",
    "gzip, deflate, br, zstd",
]

# 代理IP池（示例，实际使用时需要替换为可用的代理IP）
PROXY_POOL = [
    # 格式: "http://ip:port" 或 "http://username:password@ip:port"
    # 注意: 这些是示例，实际使用时需要替换为真实的代理IP
    # "http://123.45.67.89:8080",
    # "http://123.45.67.90:8080",
    # "http://user:pass@123.45.67.91:8080",
]

# 代理IP使用计数器
proxy_usage_count = {}
proxy_last_used = {}

def get_random_proxy():
    """随机选择一个代理IP，并考虑使用频率"""
    if not PROXY_POOL:
        return None
    
    # 过滤掉最近使用过的代理
    available_proxies = []
    current_time = time.time()
    
    for proxy in PROXY_POOL:
        # 如果代理最近没有被使用过，或者距离上次使用已经超过5分钟
        if proxy not in proxy_last_used or current_time - proxy_last_used[proxy] > 300:
            available_proxies.append(proxy)
    
    # 如果没有可用的代理，则选择使用次数最少的代理
    if not available_proxies:
        available_proxies = sorted(PROXY_POOL, key=lambda p: proxy_usage_count.get(p, 0))
    
    # 随机选择一个代理
    proxy = random.choice(available_proxies)
    
    # 更新代理使用计数和最后使用时间
    proxy_usage_count[proxy] = proxy_usage_count.get(proxy, 0) + 1
    proxy_last_used[proxy] = current_time
    
    return proxy

def get_random_headers():
    # 随机选择浏览器版本和操作系统
    browser_version, os_info = random.choice(BROWSER_VERSIONS)
    
    # 生成更真实的User-Agent
    user_agent = f"Mozilla/5.0 ({os_info}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{browser_version} Safari/537.36"
    
    # 随机选择Accept-Language
    accept_language = random.choice(ACCEPT_LANGUAGES)
    
    # 随机选择Accept-Encoding
    accept_encoding = random.choice(ACCEPT_ENCODINGS)
    
    # 生成随机的屏幕分辨率
    screen_resolution = f"{random.choice([1920, 1366, 1440, 1536])}x{random.choice([1080, 768, 900, 864])}"
    
    # 生成随机的时间偏移
    time_offset = random.randint(-300, 300)  # -5分钟到+5分钟
    
    headers = {
        "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "accept-language": accept_language,
        "accept-encoding": accept_encoding,
        "cache-control": random.choice(["max-age=0", "no-cache", "no-store"]),
        "sec-ch-ua": f'"Chromium";v="{browser_version.split(".")[0]}", "Google Chrome";v="{browser_version.split(".")[0]}", ";Not A Brand";v="99"',
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": f'"{random.choice(["Windows", "macOS", "Linux"])}"',
        "sec-fetch-dest": "document",
        "sec-fetch-mode": "navigate",
        "sec-fetch-site": "none",
        "sec-fetch-user": "?1",
        "upgrade-insecure-requests": "1",
        "user-agent": user_agent,
        "dnt": random.choice(["0", "1"]),
        "referer": "https://www.amazon.com/",
        "viewport-width": str(screen_resolution.split("x")[0]),
        "viewport-height": str(screen_resolution.split("x")[1]),
    }
    
    return headers


async def update_cookie():
    """生成更真实的Cookie"""
    current_time = int(time.time())
    
    # 生成随机的session-id
    session_id = f"{random.randint(100000000, 999999999)}-{random.randint(100000000, 999999999)}"
    
    # 生成随机的csm-hit
    csm_hit = f"tb:{random.randint(1000000, 9999999)}+s-{random.randint(1000000, 9999999)}|{current_time}"
    
    # 生成随机的ubid-main
    ubid_main = f"{random.randint(100, 999)}-{random.randint(1000000, 9999999)}-{random.randint(1000000, 9999999)}"
    
    # 生成随机的session-token
    session_token = "".join(random.choice("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/") for _ in range(160))
    
    # 生成随机的i18n-prefs
    i18n_prefs = f"USD|en-US|{random.randint(100, 999)}"
    
    # 生成随机的lc-main
    lc_main = f"en_US|{random.randint(1000000000, 9999999999)}"
    
    # 生成随机的时间戳
    time_stamp = current_time - random.randint(60, 3600)  # 1分钟到1小时前
    
    cookie = (
        f"session-id={session_id}; "
        f"session-token={session_token}; "
        f"csm-hit={csm_hit}; "
        f"ubid-main={ubid_main}; "
        f"i18n-prefs={i18n_prefs}; "
        f"lc-main={lc_main}; "
        f"session-id-time={time_stamp}; "
        f"sp-cdn=\"L5Z9:NY\""
    )
    
    return cookie

async def fetch_page(session, id, semaphore, retry_count=0):
    async with semaphore: 
        url = f"https://www.amazon.com/dp/{id}"
        headers = get_random_headers()
        headers["cookie"] = await update_cookie()
        
        try:
            async with session.get(url, headers=headers, timeout=20) as response:
                content = await response.text()
                file_path = f'../results/{id}.html'
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                
                file_size = os.path.getsize(file_path)
                if (3000 < file_size <= 9000) or "captcha" in content.lower():
                    if retry_count < MAX_RETRIES:
                        print(f"检测到反爬机制或验证码，正在重试 {id}（第 {retry_count + 1} 次）")
                        wait_time = random.uniform(5, 10) * (retry_count + 1)
                        await asyncio.sleep(wait_time)
                        return await fetch_page(session, id, semaphore, retry_count + 1)
                    else:
                        print(f"达到最大重试次数，{id} 爬取失败")
                        with open('../max_retries.txt', 'a') as f:
                            f.write(f"{id}\n")
                else:
                    print(f"已成功接收 {id} 页面")
        except Exception as e:
            with open(f'../errors/{id}.err', 'w', encoding='utf-8') as err_f:
                err_f.write(str(e))
            print(f"处理 {id} 时出错: {str(e)}")
            if retry_count < MAX_RETRIES:
                wait_time = random.uniform(5, 10) * (retry_count + 1)
                await asyncio.sleep(wait_time)
                return await fetch_page(session, id, semaphore, retry_count + 1)
            else:
                with open('../max_retries.txt', 'a') as f:
                    f.write(f"{id}\n")

async def process_batch(ids):
    # 创建 SSL 上下文
    ssl_context = ssl.create_default_context()
    # 创建本地信号量，控制最大并发数
    semaphore = asyncio.Semaphore(50)

    async with aiohttp.ClientSession(connector=aiohttp.TCPConnector(limit=200,ssl=ssl_context)) as session:
        tasks = [fetch_page(session, id.strip(), semaphore) for id in ids]
        await asyncio.gather(*tasks)
    
    await asyncio.sleep(random.uniform(1, 2))


async def main():
    # 创建必要的目录
    os.makedirs('../results', exist_ok=True)
    os.makedirs('../errors', exist_ok=True)

    # 确保 max_retries.txt 文件存在，如果不存在则创建
    if not os.path.exists('../max_retries.txt'):
        open('../max_retries.txt', 'a').close()

    with open('../unique_product_ids.txt', 'r') as ids_file:
        ids = ids_file.readlines()[START_POSITION:]

    for i in range(0, len(ids), BATCH_SIZE):
        batch = ids[i:i + BATCH_SIZE]
        await process_batch(batch)


if __name__ == "__main__":
    asyncio.run(main())
