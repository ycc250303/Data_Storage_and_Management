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
import re

MAX_WORKERS = 100 
RESOURCE_DIR = '../results'
OUTPUT_DIR = '../csvs'

def remove_brackets_content(text):
    """
    去掉字符串中的括号及括号内的内容
    支持中括号[]和小括号()
    """
    if not text:
        return text
    
    # 去掉中括号及括号内的内容
    text = re.sub(r'\[.*?\]', '', text)
    # 去掉小括号及括号内的内容
    text = re.sub(r'\(.*?\)', '', text)
    # 去掉多余的空格
    text = re.sub(r'\s+', ' ', text).strip()
    
    return text

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
        self.score = ''       # 电影评分
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
            self.score,
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
        # 获取 score 评分
        score = page.xpath('//span[@aria-hidden="true" and contains(@class, "a-size-small") and contains(@class, "a-color-base")]/text()')
        if len(score) > 0:
            info.score = score[0].strip()


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
        if genre and genre != "Featured Categories":
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

        # 获取电影标题 - Prime Video页面使用不同的选择器
        title = page.xpath('normalize-space(//h1[@data-automation-id="title"]/text())')
        if not title:
            title = page.xpath('normalize-space(//title/text())')
            if title and '|' in title:
                title = title.split('|')[0].strip()
        info.title = title

        # 获取电影语言 - Prime Video页面的语言信息
        audio_languages = page.xpath('normalize-space(//h3/span[text()="Audio languages"]/../../dd/text())')
        if not audio_languages:
            # 尝试其他可能的选择器
            audio_languages = page.xpath('normalize-space(//span[text()="Audio languages"]/../../../dd/text())')
        if audio_languages:
            info.language = remove_brackets_content(audio_languages)

        # 获取上映日期 - Prime Video页面的发布年份
        release_year = page.xpath('normalize-space(//span[@data-automation-id="release-year-badge"]/text())')
        if release_year:
            info.release_date = release_year

        # 获取电影评级 - Prime Video页面的内容评级
        rated = page.xpath('normalize-space(//span[@data-automation-id="rating-badge"]/text())')
        if rated:
            info.rated = rated

        # 获取电影演员 - Prime Video页面的演员信息
        actors_elements = page.xpath('//h3/span[text()="Cast"]/../../dd/a/text()')
        if not actors_elements:
            # 尝试其他可能的选择器
            actors_elements = page.xpath('//span[text()="Cast"]/../../../dd/a/text()')
        if actors_elements:
            info.actors = ', '.join(actors_elements)

        # 获取电影导演 - Prime Video页面的导演信息
        directors = page.xpath('//h3/span[text()="Directors"]/../../dd/a/text()')
        if not directors:
            # 尝试其他可能的选择器
            directors = page.xpath('//span[text()="Directors"]/../../../dd/a/text()')
        if directors:
            info.director = ', '.join(directors)

        # 获取电影评分 - Prime Video页面的星级评分
        div_elements = page.xpath('//div[@aria-label]')
        for div in div_elements:
            aria_label = div.get('aria-label', '')
            if 'Rated' in aria_label and 'out of 5 stars' in aria_label:
                # 提取评分数字，例如从"Rated 2.8 out of 5 stars by 11 Amazon customers."中提取"2.8"
                match = re.search(r'Rated (\d+\.\d+) out of 5 stars', aria_label)
                if match:
                    info.score = match.group(1)
                break

        # 获取电影其他版本 - Prime Video页面的其他格式
        # 尝试多种选择器以确保获取所有版本
        other_formats = page.xpath('//div[@data-automation-id="other-formats"]//strong/text()')
        if not other_formats:
            other_formats = page.xpath('//h3[text()="Other formats"]/following-sibling::div//strong/text()')
        if not other_formats:
            other_formats = page.xpath('//h3[contains(text(), "Other formats")]/following-sibling::div//a//strong/text()')
        
        for format_name in other_formats:
            if format_name and format_name.strip():
                info.editions.append(format_name.strip())

        # 获取同一部电影的不同id - Prime Video页面的相关ID
        # 只从"Other formats"部分提取相关ID，确保准确性
        other_format_links = page.xpath('//div[@data-automation-id="other-formats"]//a[contains(@href, "/dp/")]/@href')
        if not other_format_links:
            other_format_links = page.xpath('//h3[text()="Other formats"]/following-sibling::div//a[contains(@href, "/dp/")]/@href')
        
        for href in other_format_links:
            if '/dp/' in href:
                try:
                    related_id = href.split('/dp/')[1][:10]
                    # 确保ID是10位字母数字组合
                    if len(related_id) == 10 and related_id.isalnum():
                        info.related_ids.add(related_id)
                except:
                    pass

        # 缺失数据过多则可能不是电影
        if info.isMovie():
            with open(os.path.join(self.target_dir, asin), 'a', encoding='utf-8', newline='') as csv_file:
                writer = csv.writer(csv_file)
                writer.writerow(info.tovec())

def main():
    parser = Parser(OUTPUT_DIR)
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        count = 0
        for file_name in os.listdir(RESOURCE_DIR):
            # if count >= 1000:
            #     break
            executor.submit(parser.parse, RESOURCE_DIR, file_name)
            count += 1

if __name__ == '__main__':
    main()
