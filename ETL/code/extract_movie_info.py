# 涉及的页面： 
# 1. 白底商品页面
# 2. 其他页面
# 
# 黑底页面全部处理，其他页面均不处理
# 白底页面需要先筛选，筛出除 Movies&TV 以外的页面（如 CD）

from lxml import etree
from concurrent.futures import ThreadPoolExecutor
import os
import csv

MAX_WORKERS = 100 
RESOURCE_DIR = '../results'
OUTPUT_DIR = '../csvs'

class MovieInfo:
    def __init__(self, asin):
        self.asin = asin       # 电影 ID
        self.title = ''        # 电影名
        self.language = ''     # 电影语言
        self.release_date = '' # 上映日期
        self.rated = ''        # 电影评级
        self.actors = ''       # 电影演员
        self.director = ''     # 电影导演
        self.genres = ''       # 电影风格
        self.rating = ''       # 电影评分
        self.editions = []     # 电影版本（媒体格式）
        self.related_ids = set() # 相关 ID

    def tovec(self):
        return [
            # 电影ID
            self.asin,
            # 电影标题
            self.title,
            # 电影语言
            self.language,
            # 上映日期
            self.release_date,
            # 电影评级
            self.rated,
            # 电影演员
            self.actors,
            # 电影导演
            self.director,
            # 电影风格
            self.genres,
            # 电影评分
            self.rating,
            # 电影版本
            ','.join(self.editions),
        ]

    def isMovie(self):
        counter = 0
        if self.release_date == '':
            counter += 1
        if self.actors == '':
            counter += 1
        if self.director == '':
            counter += 1
        if self.genres == '':
            counter += 1
        if self.language == '':
            counter += 1
        if self.rated == '':
            counter += 1
        return 6 - counter > 2

class Parser:
    def __init__(self, target_dir):
        self.target_dir = target_dir
        if not os.path.exists(target_dir):
            os.mkdir(target_dir)

    def parse(self, file_path, file_name):
        with open(os.path.join(file_path, file_name), 'r', encoding='utf-8') as f:
            content = f.read()

        root = etree.HTML(content)

        # Movies & TV
        # Prime Video
        prime_label = root.xpath('normalize-space(//div[@id="pv-nav-container"]/div/a/img/@alt)')
        if 'Prime Video' in prime_label:
            print(f"{file_name} Prime")
            self.parsePrimePage(root, file_name.split('.')[0])
            return 
        movie_label = root.xpath('normalize-space(//*[@id="wayfinding-breadcrumbs_feature_div"]/ul/li[1]/span/a/text())')
        if 'Movies & TV' in movie_label:
            print(f"{file_name} Movie!")
            self.parseMoviePage(root, file_name.split('.')[0])
            return
      
        else:
            print(f"{file_name} other!")

    def parseMoviePage(self, page, asin):
        info = MovieInfo(asin)

        # 获取电影标题
        info.title = page.xpath('normalize-space(//span[@id="productTitle"]/text())')

        # 获取电影版本
        movie_edition = page.xpath('normalize-space(//*[@id="declarative_"]/table/tbody/tr/td[2]/div/span/text())')
        if movie_edition and movie_edition != '':
            info.editions.append(movie_edition)

        # 获取电影评级
        rated = page.xpath('//*[@id="bylineInfo"]/div/div/span[@class="a-size-small"]/text()')
        if rated == None or len(rated) == 0:
            rated = page.xpath('//*[@id="bylineInfo_feature_div"]/div/div/span[@class="a-size-small"]/text()')
        if len(rated) > 0:
            info.rated = rated[0]
        # 获取 rating 评分
        rating = page.xpath('//span[@aria-hidden="true" and contains(@class, "a-size-small") and contains(@class, "a-color-base")]/text()')
        if len(rating) > 0:
            info.rating = rating[0].strip()


        # 获取details并抽取相关信息
        movie_starring = ''
        movie_details = page.xpath('//*[@id="detailBullets_feature_div"]/ul/li/span')
        for detail in movie_details:
            key = detail.xpath('normalize-space(.//span[1]/text())')
            value = detail.xpath('normalize-space(.//span[2]/text())')
            if 'Release date' in key:
                info.release_date = value
            elif 'Starring' in key:
                movie_starring = value
            elif 'Actors' in key:
                info.actors = value
            elif 'Director' in key:
                info.director = value
            elif 'Format' in key:
                info.editions.append(value)

        # 获取电影风格
        genre = page.xpath('normalize-space(//*[@id="wayfinding-breadcrumbs_feature_div"]//a[@aria-current="page"]/text())')
        if genre:
            info.genres = genre

        # 获取电影语言
        language = page.xpath('normalize-space(//tr[contains(@class,"po-language")]//td[2]/span/text())')
        if language:
            info.language = language

        # 获取同一部电影的不同id
        hrefs = page.xpath('//*[@id="tmmSwatches"]/ul/li//a[contains(@href, "/dp/")]/@href')
        for href in hrefs:
            info.related_ids.add(href.split('/dp/')[1][:10])

        # 缺失数据过多则可能不是电影
        if info.isMovie():
            with open(os.path.join(self.target_dir, asin), 'a', encoding='utf-8', newline='') as csv_file:
            # with open(self.target_file, 'a', encoding='utf-8', newline='') as csv_file:
                writer = csv.writer(csv_file)
                writer.writerow(info.tovec())

    def parsePrimePage(self, page, asin):
        info = MovieInfo(asin)

        # 获取电影标题
        info.title = page.xpath('normalize-space(//span[@id="productTitle"]/text())')

        # 获取电影版本
        movie_edition = page.xpath('normalize-space(//*[@id="declarative_"]/table/tbody/tr/td[2]/div/span/text())')
        if movie_edition and movie_edition != '':
            info.editions.append(movie_edition)

        # 获取电影评级
        rated = page.xpath('//*[@id="bylineInfo"]/div/div/span[@class="a-size-small"]/text()')
        if rated == None or len(rated) == 0:
            rated = page.xpath('//*[@id="bylineInfo_feature_div"]/div/div/span[@class="a-size-small"]/text()')
        if len(rated) > 0:
            info.rated = rated[0]
        # 获取 IMDb 评分
        imdb = page.xpath('//span[@class="imdb-rating"]/strong/text()')
        if len(imdb) > 0:
            info.imdb = imdb[0]

        # 获取电影风格并补充定影版本信息
        tbody_elements = page.xpath('//*[@id="productOverview_feature_div"]/div/table/tbody')  # 获取tbody元素
        if tbody_elements:
            tr_elements = tbody_elements[0].xpath('.//tr')  # 获取所有行
            for tr_element in tr_elements:  # 处理每一行中的所有列
                element1 = tr_element.xpath('normalize-space(.//td[1]/span/text())')
                element2 = tr_element.xpath('normalize-space(.//td[2]/span/text())')
                if element1 == 'Genre':
                    info.genres = element2
                elif element1 == 'Language':
                    info.language = element2

        # 获取details并抽取相关信息
        movie_starring = ''
        movie_details = page.xpath('//*[@id="detailBullets_feature_div"]/ul/li/span')
        for detail in movie_details:
            key = detail.xpath('normalize-space(.//span[1]/text())')
            value = detail.xpath('normalize-space(.//span[2]/text())')
            if 'Release date' in key:
                info.release_date = value
            elif 'Starring' in key:
                movie_starring = value
            elif 'Actors' in key:
                info.actors = value
            elif 'Director' in key:
                info.director = value
            elif 'Format' in key:
                info.editions.append(value)

        # 获取同一部电影的不同id
        hrefs = page.xpath('//*[@id="tmmSwatches"]/ul/li//a[contains(@href, "/dp/")]/@href')
        for href in hrefs:
            info.related_ids.add(href.split('/dp/')[1][:10])

        # 缺失数据过多则可能不是电影
        if info.isMovie():
            with open(os.path.join(self.target_dir, asin), 'a', encoding='utf-8', newline='') as csv_file:
            # with open(self.target_file, 'a', encoding='utf-8', newline='') as csv_file:
                writer = csv.writer(csv_file)
                writer.writerow(info.tovec())

def main():
    parser = Parser(OUTPUT_DIR)
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        count = 0
        for file_name in os.listdir(RESOURCE_DIR):
            if count >= 10000:
                break
            executor.submit(parser.parse, RESOURCE_DIR, file_name)
            count += 1

if __name__ == '__main__':
    main()
