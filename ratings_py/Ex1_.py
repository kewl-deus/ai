#!/usr/bin/env python
# coding: utf-8

# Importing Libraries

# In[1]:


import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import time
from collections import deque
import tensorflow as tf
from six import next


# Main Functions For Reading and Splitting the Data

# In[2]:


def read_process(filname, sep="\t"):
    col_names = ["user", "item", "rate", "st"]
    df = pd.read_csv(filname, sep=sep, header=None, names=col_names, engine='python')
    df["user"] -= 1
    df["item"] -= 1
    for col in ("user", "item"):
        df[col] = df[col].astype(np.int32)
    df["rate"] = df["rate"].astype(np.float32)
    return df

def get_split_data():
    df_train = read_process("Data/ml100k/u1.base", sep="\t")
    df_test = read_process("Data/ml100k/u1.test", sep="\t")
    return df_train, df_test


# Reading Data into Tables aka Dataframes
# For more infor about pandas Dataframse check the below link
# https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.DataFrame.html
# 

# In[3]:


df_train, df_test = get_split_data()


# In[21]:


itemAvg = df_train.groupby(['item'])['rate'].mean()
itemAvg.iloc[1:5]


# In[24]:


errors = deque()
itemPred = itemAvg.values

for index, row in df_test.iterrows():
    test_itemid = int(row['item'])
    test_rate = float(row['rate'])
    
    errors.append(np.power(itemPred[test_itemid] - test_rate, 2))
    
test_err = np.sqrt(np.mean(errors))
test_err


# In[ ]:




