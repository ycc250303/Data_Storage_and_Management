import datetime
import pandas as pd

REVIEW_FILE = "../earliest_review_time.csv"
INPUT_MOVIE_FILE = "../movie_info_merged.csv"
OUTPUT_MOVIE_FILE = "../movie_info_final.csv"

def addReleaseDate():
    output = pd.read_csv(INPUT_MOVIE_FILE, encoding="utf-8")

    review_time = pd.read_csv(REVIEW_FILE, encoding="utf-8")

    review_time_dict = review_time.set_index("productID")["reviewTime"].to_dict()

    def convert_timestamp_to_date(timestamp):
        return datetime.datetime.utcfromtimestamp(timestamp).strftime("%Y-%m-%d")

    for index, row in output.iterrows():
        if pd.isnull(row["ReleaseDate"]):
            product_id = row["ASIN"]
            if product_id in review_time_dict:
                review_time = review_time_dict[product_id]
                formatted_date = convert_timestamp_to_date(review_time)
                output.at[index, "Release date"] = formatted_date

    output.to_csv(OUTPUT_MOVIE_FILE, index=False, encoding="utf-8", quoting=3)

addReleaseDate()
print("电影上映时间补充完成")
