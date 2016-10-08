#!/usr/bin/env python

from os import path
from wordcloud import WordCloud
import matplotlib.pyplot as plt

d = path.dirname(__file__)

# Read the whole text.
text = open(path.join(d, 'data/all-words.txt')).read()

# Generate a word cloud image
wordcloud = WordCloud().generate(text)

wordcloud = WordCloud(relative_scaling=.5).generate(text)
plt.figure()
plt.imshow(wordcloud)
plt.axis("off")
plt.show()
