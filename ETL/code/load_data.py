import aiomysql
import asyncio
import uuid
import csv
import random
import warnings
from datetime import datetime

# 过滤 MySQL 重复键警告
warnings.filterwarnings('ignore', message='.*Duplicate entry.*')

MOVIE_INFO_DATA_FILE = "../../data/etl/movie_info_final.csv"
MOVIE_REVIEW_NUM_FILE = "../../data/etl/review_num.csv"
MOVIE_REVIEW_INFO_FILE = "../../movies.txt"

# 异步模式配置（优化后，平衡性能与稳定性）
ASYNC_MAX_CONCURRENT = 15  # 最大并发数
ASYNC_POOL_SIZE = 15  # 异步连接池大小
ASYNC_BATCH_SIZE = 50  # 每批处理的数据量
MAX_RETRY = 5  # 最大重试次数（适用于死锁和锁超时）

def split_list(s):
    """分割逗号或分号分隔的字符串"""
    if not s:
        return []
    # 首先按分号分割，然后对每个部分再按逗号分割
    parts = []
    for part in s.split(';'):
        for subpart in part.split(','):
            item = subpart.strip()
            if item:
                parts.append(item)
    return parts

def safe_float(value, default=0.0):
    """安全转换为浮点数"""
    if value is None:
        return default
    if isinstance(value, (int, float)):
        return float(value)
    try:
        s = str(value).strip()
        if s == "":
            return default
        if 'out of' in s:
            s = s.split('out of')[0].strip()
        return float(s)
    except Exception:
        return default

def safe_int(value, default=0):
    """安全转换为整数（支持小数格式如 '5.0'）"""
    if value is None:
        return default
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        return int(value)
    try:
        s = str(value).strip()
        if s == "":
            return default
        # 先转换为浮点数，再转换为整数（处理 '5.0' 这种情况）
        return int(float(s))
    except Exception:
        return default


class AsyncLoadDataTool:
    """异步并发数据加载工具（仿照爬虫的异步模式）"""
    
    def __init__(self):
        self.progress_count = 0
        self.failed_count = 0
        self.retry_count = 0
        self.lock = asyncio.Lock()
        self.pool = None
        
    async def create_pool(self):
        """创建异步连接池"""
        self.pool = await aiomysql.create_pool(
            host='111.229.81.45',
            port=3306,
            user='data_warehouse',
            password='123456',
            db='data_warehouse',
            charset='utf8mb4',
            autocommit=False,
            minsize=10,
            maxsize=ASYNC_POOL_SIZE,
        )
        
    async def close_pool(self):
        """关闭连接池"""
        if self.pool:
            self.pool.close()
            await self.pool.wait_closed()
    
    # ================= 异步数据库操作 =================
    async def loadActor(self, name, conn):
        """异步加载演员"""
        async with conn.cursor() as cursor:
            sql = """
            INSERT INTO actors(name)
            VALUES (%s)
            ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
            """
            await cursor.execute(sql, (name,))
            return cursor.lastrowid
    
    async def loadActors(self, names, conn):
        """批量加载演员"""
        if not names:
            return []
        actor_ids = []
        for name in split_list(names):
            actor_id = await self.loadActor(name, conn)
            actor_ids.append(actor_id)
        return actor_ids
    
    async def loadDirector(self, name, conn):
        """异步加载导演"""
        async with conn.cursor() as cursor:
            sql = """
            INSERT INTO directors(name)
            VALUES (%s)
            ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
            """
            await cursor.execute(sql, (name,))
            return cursor.lastrowid
    
    async def loadDirectors(self, names, conn):
        """批量加载导演"""
        if not names:
            return []
        director_ids = []
        for name in split_list(names):
            director_id = await self.loadDirector(name, conn)
            director_ids.append(director_id)
        return director_ids
    
    async def loadMovie(self, asin, title, score, rated, language, conn):
        """异步加载电影"""
        async with conn.cursor() as cursor:
            sql = """
            INSERT INTO movies(movie_asin, movie_title, score, rated, language)
            VALUES (%s,%s,%s,%s,%s)
            ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
            """
            await cursor.execute(sql, (asin, title, score, rated, language))
            return cursor.lastrowid
    
    async def batchInsert(self, sql, data, conn):
        """批量插入"""
        if data:
            async with conn.cursor() as cursor:
                await cursor.executemany(sql, data)
    
    async def loadMovieRelations(self, movie_id, actor_ids, director_ids, genres, editions, conn):
        """批量加载电影关联数据"""
        # 演员关系
        if actor_ids:
            sql = "INSERT IGNORE INTO movie_actors(movie_id, actor_id) VALUES (%s,%s)"
            await self.batchInsert(sql, [(movie_id, aid) for aid in actor_ids], conn)
        
        # 导演关系
        if director_ids:
            sql = "INSERT IGNORE INTO movie_directors(movie_id, director_id) VALUES (%s,%s)"
            await self.batchInsert(sql, [(movie_id, did) for did in director_ids], conn)
        
        # 类型
        genre_list = split_list(genres)
        if genre_list:
            sql = "INSERT IGNORE INTO movie_genres(movie_id, genre) VALUES (%s,%s)"
            await self.batchInsert(sql, [(movie_id, g) for g in genre_list], conn)
        
        # 版本
        edition_list = split_list(editions)
        if edition_list:
            sql = "INSERT IGNORE INTO movie_editions(movie_id, edition) VALUES (%s,%s)"
            await self.batchInsert(sql, [(movie_id, e) for e in edition_list], conn)
    
    async def loadReleaseDate(self, movie_id, date_str, conn):
        """异步加载发行日期"""
        if not date_str:
            return
        try:
            dt = datetime.strptime(date_str.strip(), "%B %d, %Y")
            year, month, day, weekday = dt.year, dt.month, dt.day, dt.weekday()
            
            async with conn.cursor() as cursor:
                sql = """
                INSERT IGNORE INTO release_dates(movie_id, year, month, day, weekday)
                VALUES (%s,%s,%s,%s,%s)
                """
                await cursor.execute(sql, (movie_id, year, month, day, weekday))
        except:
            pass
    
    async def getMovieIdByAsin(self, asin, conn):
        """异步根据ASIN获取电影ID"""
        async with conn.cursor() as cursor:
            await cursor.execute("SELECT id FROM movies WHERE movie_asin=%s", (asin,))
            row = await cursor.fetchone()
            return row[0] if row else None
    
    async def updateMovieReviewNum(self, asin, review_num, conn):
        """异步更新电影评论数量"""
        async with conn.cursor() as cursor:
            sql = """
            UPDATE movies SET review_num = %s
            WHERE movie_asin = %s
            """
            await cursor.execute(sql, (review_num, asin))
            return cursor.rowcount > 0
    
    async def loadMovieReview(self, review, conn, asin_map=None):
        """异步加载单个电影评论"""
        asin = review.get('product/productId')
        
        # 优先使用映射字典，避免查询数据库
        if asin_map is not None:
            movie_id = asin_map.get(asin)
        else:
            movie_id = await self.getMovieIdByAsin(asin, conn)
        
        if not movie_id:
            return False
        
        async with conn.cursor() as cursor:
            sql = """
            INSERT INTO reviews(
                review_uuid, movie_id, user_id, profile_name,
                helpfulness, score, created_ts, summary, content
            )
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)
            """
            await cursor.execute(sql, (
                uuid.uuid4().bytes,
                movie_id,
                review.get('review/userId'),
                review.get('review/profileName'),
                review.get('review/helpfulness'),
                safe_int(review.get('review/score', 0)),
                review.get('review/time', 0),
                review.get('review/summary'),
                review.get('review/text')
            ))
            return True
    
    def parseMovieReview(self, file_path):
        """解析电影评论文件（生成器）"""
        current = {}
        with open(file_path, 'r', encoding='latin-1', errors='replace') as f:
            for line in f:
                line = line.strip()
                if not line:
                    if current:
                        yield current
                        current = {}
                    continue
                if ':' in line:
                    k, v = line.split(':', 1)
                    current[k.strip()] = v.strip()
        if current:
            yield current
    
    def countReviews(self, file_path):
        """快速统计评论总数"""
        count = 0
        current_has_data = False
        with open(file_path, 'r', encoding='latin-1', errors='replace') as f:
            for line in f:
                line = line.strip()
                if not line:
                    if current_has_data:
                        count += 1
                        current_has_data = False
                elif ':' in line:
                    current_has_data = True
        if current_has_data:
            count += 1
        return count
    
    async def loadOneMovie(self, row, semaphore, retry_count=0):
        """异步加载单个电影（类似爬虫的 fetch_page）- 支持死锁重试"""
        async with semaphore:  # 控制并发数
            async with self.pool.acquire() as conn:
                try:
                    # 加载电影基本信息
                    movie_id = await self.loadMovie(
                        row["ASIN"], row["Title"], safe_float(row["Score"]),
                        row["Rated"], row["Language"], conn
                    )
                    
                    # 加载演员和导演
                    actor_ids = await self.loadActors(row["Actors"], conn)
                    director_ids = await self.loadDirectors(row["Directors"], conn)
                    
                    # 批量加载关联数据
                    await self.loadMovieRelations(
                        movie_id, actor_ids, director_ids,
                        row["Genres"], row["Editions"], conn
                    )
                    
                    # 加载发行日期
                    await self.loadReleaseDate(movie_id, row["ReleaseDate"], conn)
                    
                    # 提交事务
                    await conn.commit()
                    
                    # 更新进度
                    async with self.lock:
                        self.progress_count += 1
                        if self.progress_count % 100 == 0:
                            print(f"⏳ 已处理: {self.progress_count} 部电影")
                    
                    return True
                    
                except Exception as e:
                    await conn.rollback()
                    
                    # 检查是否是死锁错误 (1213)
                    error_code = getattr(e, 'args', [None])[0] if hasattr(e, 'args') else None
                    if error_code == 1213 and retry_count < MAX_RETRY:
                        # 记录重试
                        async with self.lock:
                            self.retry_count += 1
                        
                        # 死锁错误，自动重试（指数退避 + 随机抖动）
                        base_wait = 0.5 * (2 ** retry_count)  # 0.5s, 1s, 2s, 4s, 8s
                        jitter = random.uniform(0, 0.5)  # 随机抖动，避免重试风暴
                        wait_time = base_wait + jitter
                        await asyncio.sleep(wait_time)
                        # 递归重试
                        return await self.loadOneMovie(row, semaphore, retry_count + 1)
                    else:
                        # 其他错误或重试次数已达上限
                        async with self.lock:
                            self.failed_count += 1
                        
                        if error_code == 1213:
                            print(f"❌ 电影 {row.get('ASIN', 'Unknown')} 处理失败（死锁，已重试{MAX_RETRY}次）")
                        else:
                            print(f"❌ 电影 {row.get('ASIN', 'Unknown')} 处理失败: {e}")
                        return False
    
    async def loadOneReviewNum(self, row, semaphore, retry_count=0):
        """异步加载单个电影评论数量 - 支持死锁重试"""
        async with semaphore:
            async with self.pool.acquire() as conn:
                try:
                    success = await self.updateMovieReviewNum(
                        row["productID"], row["review_num"], conn
                    )
                    
                    await conn.commit()
                    
                    if success:
                        async with self.lock:
                            self.progress_count += 1
                            if self.progress_count % 100 == 0:
                                print(f"⏳ 已更新: {self.progress_count} 部电影评论数")
                    
                    return True
                    
                except Exception as e:
                    await conn.rollback()
                    
                    error_code = getattr(e, 'args', [None])[0] if hasattr(e, 'args') else None
                    if error_code == 1213 and retry_count < MAX_RETRY:
                        async with self.lock:
                            self.retry_count += 1
                        
                        base_wait = 0.5 * (2 ** retry_count)
                        jitter = random.uniform(0, 0.5)
                        wait_time = base_wait + jitter
                        await asyncio.sleep(wait_time)
                        return await self.loadOneReviewNum(row, semaphore, retry_count + 1)
                    else:
                        async with self.lock:
                            self.failed_count += 1
                        
                        if error_code == 1213:
                            print(f"❌ 评论数更新失败（死锁，已重试{MAX_RETRY}次）: {row.get('productID', 'Unknown')}")
                        else:
                            print(f"❌ 评论数更新失败: {row.get('productID', 'Unknown')} - {e}")
                        return False
    
    async def loadOneReview(self, review, semaphore, asin_map=None, retry_count=0):
        """异步加载单个评论 - 支持死锁重试"""
        async with semaphore:
            async with self.pool.acquire() as conn:
                try:
                    success = await self.loadMovieReview(review, conn, asin_map)
                    
                    await conn.commit()
                    
                    if success:
                        async with self.lock:
                            self.progress_count += 1
                            # 增加进度输出频率，让用户知道程序在运行
                            if self.progress_count % 500 == 0:
                                print(f"⏳ 已加载: {self.progress_count} 条评论")
                    
                    return True
                    
                except Exception as e:
                    await conn.rollback()
                    
                    error_code = getattr(e, 'args', [None])[0] if hasattr(e, 'args') else None
                    if error_code == 1213 and retry_count < MAX_RETRY:
                        async with self.lock:
                            self.retry_count += 1
                        
                        base_wait = 0.5 * (2 ** retry_count)
                        jitter = random.uniform(0, 0.5)
                        wait_time = base_wait + jitter
                        await asyncio.sleep(wait_time)
                        return await self.loadOneReview(review, semaphore, asin_map, retry_count + 1)
                    else:
                        async with self.lock:
                            self.failed_count += 1
                        
                        if error_code == 1213:
                            print(f"❌ 评论加载失败（死锁，已重试{MAX_RETRY}次）: {review.get('product/productId', 'Unknown')}")
                        else:
                            print(f"❌ 评论加载失败: {review.get('product/productId', 'Unknown')} - {e}")
                        return False
    
    async def processBatch(self, batch, semaphore):
        """处理一批数据（类似爬虫的 process_batch）"""
        tasks = [self.loadOneMovie(row, semaphore) for row in batch]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        return sum(1 for r in results if r is True)
    
    async def processReviewNumBatch(self, batch, semaphore):
        """处理一批评论数量数据"""
        tasks = [self.loadOneReviewNum(row, semaphore) for row in batch]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        return sum(1 for r in results if r is True)
    
    async def processReviewBatch(self, batch, semaphore, asin_map=None):
        """处理一批评论数据"""
        tasks = [self.loadOneReview(review, semaphore, asin_map) for review in batch]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        return sum(1 for r in results if r is True)
    
    async def buildAsinToMovieIdMap(self):
        """构建ASIN到movie_id的映射字典（避免重复查询）"""
        print(f"📦 正在构建ASIN映射字典...")
        asin_map = {}
        async with self.pool.acquire() as conn:
            async with conn.cursor() as cursor:
                await cursor.execute("SELECT id, movie_asin FROM movies")
                rows = await cursor.fetchall()
                for row in rows:
                    asin_map[row[1]] = row[0]
        print(f"✅ 映射字典构建完成，共 {len(asin_map)} 部电影\n")
        return asin_map
    
    async def loadDataAsync(self, max_movies=None, max_review_nums=None, max_reviews=None):
        """异步加载数据（主函数，类似爬虫的 main）"""
        print(f"\n{'='*60}")
        print(f"🚀 异步并发数据加载工具")
        print(f"{'='*60}")
        print(f"📊 配置参数:")
        print(f"   - 最大并发数: {ASYNC_MAX_CONCURRENT}")
        print(f"   - 连接池大小: {ASYNC_POOL_SIZE}")
        print(f"   - 批处理大小: {ASYNC_BATCH_SIZE}")
        print(f"   - 最大重试次数: {MAX_RETRY}")
        
        # 创建连接池
        await self.create_pool()
        
        # 创建信号量（类似爬虫）
        semaphore = asyncio.Semaphore(ASYNC_MAX_CONCURRENT)
        
        # ================= 加载电影数据 =================
        if max_movies is None or max_movies > 0:
            print(f"\n{'='*60}")
            print(f"📽️  第一步：加载电影信息")
            print(f"{'='*60}")
            print(f"📖 正在读取电影数据...")
            rows = []
            with open(MOVIE_INFO_DATA_FILE, 'r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                for i, row in enumerate(reader, 1):
                    rows.append(dict(row))
                    if max_movies and i >= max_movies:
                        break
            
            print(f"✅ 共读取 {len(rows)} 部电影数据\n")
            
            # 重置计数器
            self.progress_count = 0
            self.failed_count = 0
            self.retry_count = 0
            
            # 分批处理
            total_success = 0
            
            for i in range(0, len(rows), ASYNC_BATCH_SIZE):
                batch = rows[i:i + ASYNC_BATCH_SIZE]
                success_count = await self.processBatch(batch, semaphore)
                total_success += success_count
                
                # 批次间短暂休息（避免数据库过载）
                if i + ASYNC_BATCH_SIZE < len(rows):
                    await asyncio.sleep(0.05)
            
            print(f"\n{'='*60}")
            print(f"✅ 电影数据加载完成！")
            print(f"{'='*60}")
            print(f"📊 统计信息:")
            print(f"   - 总数: {len(rows)}")
            print(f"   - 成功: {total_success}")
            if self.failed_count > 0:
                print(f"   - 失败: {self.failed_count}")
            if self.retry_count > 0:
                print(f"   - 重试次数: {self.retry_count}")
            success_rate = (total_success / len(rows) * 100) if len(rows) > 0 else 0
            print(f"   - 成功率: {success_rate:.2f}%")
            print(f"{'='*60}")
        
        # ================= 加载评论数量数据 =================
        if max_review_nums is None or max_review_nums > 0:
            print(f"\n{'='*60}")
            print(f"🔢 第二步：更新电影评论数量")
            print(f"{'='*60}")
            print(f"📖 正在读取评论数量数据...")
            
            review_num_rows = []
            with open(MOVIE_REVIEW_NUM_FILE, 'r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                for i, row in enumerate(reader, 1):
                    review_num_rows.append(dict(row))
                    if max_review_nums and i >= max_review_nums:
                        break
            
            print(f"✅ 共读取 {len(review_num_rows)} 条评论数量数据\n")
            
            # 重置计数器
            self.progress_count = 0
            self.failed_count = 0
            self.retry_count = 0
            
            total_success = 0
            
            for i in range(0, len(review_num_rows), ASYNC_BATCH_SIZE):
                batch = review_num_rows[i:i + ASYNC_BATCH_SIZE]
                success_count = await self.processReviewNumBatch(batch, semaphore)
                total_success += success_count
                
                if i + ASYNC_BATCH_SIZE < len(review_num_rows):
                    await asyncio.sleep(0.05)
            
            print(f"\n{'='*60}")
            print(f"✅ 评论数量更新完成！")
            print(f"{'='*60}")
            print(f"📊 统计信息:")
            print(f"   - 总数: {len(review_num_rows)}")
            print(f"   - 成功: {total_success}")
            if self.failed_count > 0:
                print(f"   - 失败: {self.failed_count}")
            if self.retry_count > 0:
                print(f"   - 重试次数: {self.retry_count}")
            success_rate = (total_success / len(review_num_rows) * 100) if len(review_num_rows) > 0 else 0
            print(f"   - 成功率: {success_rate:.2f}%")
            print(f"{'='*60}")
        
        # ================= 加载评论信息 =================
        if max_reviews is None or max_reviews > 0:
            print(f"\n{'='*60}")
            print(f"💬 第三步：加载电影评论")
            print(f"{'='*60}")
            
            # 构建ASIN映射字典（关键优化：避免790万次查询）
            asin_map = await self.buildAsinToMovieIdMap()
            
            # 统计评论总数
            print(f"📊 正在统计评论总数...")
            total_reviews_in_file = self.countReviews(MOVIE_REVIEW_INFO_FILE)
            expected_count = min(max_reviews, total_reviews_in_file) if max_reviews else total_reviews_in_file
            print(f"✅ 文件中共有 {total_reviews_in_file:,} 条评论")
            if max_reviews:
                print(f"   本次将处理 {expected_count:,} 条评论\n")
            else:
                print(f"   本次将处理全部评论\n")
            
            print(f"📖 开始流式处理评论数据...")
            
            # 重置计数器
            self.progress_count = 0
            self.failed_count = 0
            self.retry_count = 0
            
            total_success = 0
            total_count = 0
            batch = []
            
            # 流式处理（不一次性加载到内存）
            for review in self.parseMovieReview(MOVIE_REVIEW_INFO_FILE):
                batch.append(review)
                total_count += 1
                
                # 达到批处理大小时处理一批
                if len(batch) >= ASYNC_BATCH_SIZE:
                    success_count = await self.processReviewBatch(batch, semaphore, asin_map)
                    total_success += success_count
                    batch = []
                    await asyncio.sleep(0.05)
                
                # 达到最大处理数量时退出
                if max_reviews and total_count >= max_reviews:
                    break
            
            # 处理最后一批
            if batch:
                success_count = await self.processReviewBatch(batch, semaphore, asin_map)
                total_success += success_count
            
            print(f"\n{'='*60}")
            print(f"✅ 评论数据加载完成！")
            print(f"{'='*60}")
            print(f"📊 统计信息:")
            print(f"   - 总数: {total_count}")
            print(f"   - 成功: {total_success}")
            if self.failed_count > 0:
                print(f"   - 失败: {self.failed_count}")
            if self.retry_count > 0:
                print(f"   - 重试次数: {self.retry_count}")
            success_rate = (total_success / total_count * 100) if total_count > 0 else 0
            print(f"   - 成功率: {success_rate:.2f}%")
            print(f"{'='*60}")
        
        # 关闭连接池
        await self.close_pool()


if __name__ == "__main__":
    import time
    
    start_time = time.time()
    
    try:
        loader = AsyncLoadDataTool()
        
        # 参数说明：
        # max_movies=None 表示加载所有电影数据
        # max_review_nums=None 表示更新所有电影的评论数量
        # max_reviews=None 表示加载所有评论数据
        # 设置为 0 表示跳过该步骤
        asyncio.run(loader.loadDataAsync(
            max_movies=0,       # 加载所有电影
            max_review_nums=0,  # 更新所有评论数量
            max_reviews=None       # 加载所有评论
        ))
        
        elapsed = time.time() - start_time
        
        print(f"\n{'='*60}")
        print(f"⏱️  总体性能统计")
        print(f"{'='*60}")
        print(f"   - 总耗时: {elapsed:.2f} 秒 ({elapsed/60:.2f} 分钟)")
        if elapsed > 0:
            print(f"   - 平均处理速度: {loader.progress_count / elapsed:.2f} 条/秒")
        print(f"{'='*60}")
        
    except KeyboardInterrupt:
        print(f"\n⚠️  用户中断，正在退出...")
        print(f"   已处理: {loader.progress_count} 条数据")
    except Exception as e:
        print(f"\n❌ ETL 失败: {e}")
        import traceback
        traceback.print_exc()

