import pandas as pd
import re

df = pd.read_csv('../movie_info.csv')

def has_quotes(s):
    return isinstance(s, str) and ('"' in s or "'" in s)

print(df['Title'].apply(has_quotes).sum())

df = pd.read_csv('../movie_info_title_cleaned.csv')

# 找 Title 重复的电影
dup_titles = df[df.duplicated(subset=['Title'], keep=False)]

print("Title 重复的行数:", len(dup_titles))
print("Title+Directors 重复的行数:",
      dup_titles.duplicated(subset=['Title','Directors']).sum())
