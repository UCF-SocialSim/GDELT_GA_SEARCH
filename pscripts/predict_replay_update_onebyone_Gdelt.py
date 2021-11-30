"""
 * GDELT_GA_SEARCH: GDELT Genetic Algorithm Search Tool
 *  
 * Copyright (C) 2021  John T. Murphy
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * This code was authored by John T. Murphy with contributions from Awrad Emad
 * Harleen Lappano, and Lindsey Andrade
 * 
 * If you use this code or the tool in your work, please cite using the following bibtex:
 * @book{murphyAndWadsworth2021,
 *   author =       {Murphy, John T., and Wadsworth, Marin},
 *   title =        {GDELT GA Search Users Manual},
 *   year =         {2021},
 *   url =          {http://USER_MANUAL_URL}
 * }
"""
import sys
from sklearn.linear_model import LinearRegression
import numpy as np
from pandas import DataFrame
import pandas as pd
from sklearn import datasets, ensemble
import math



def replay(X_train, X_test, y_train):
    X_train = DataFrame(X_train,columns=['event'])
    y_train = DataFrame(y_train,columns=['event'])
    X_test = DataFrame(X_test,columns=['event'])
    init_x = X_train.values[-1]
    init_y = y_train.values[-1]


    # for intialization I am using the last training data point to predict the first y_pred in the testing period
    count_min = Ecludian_min(init_x, init_y, X_train, y_train, X_test)
    y_pred = float(y_train.values[count_min+1])
    # print("before my function", count_min)
    final_list = []
    # final_list.append(y_pred)

    # compare agenist the traning data and pull the value after the min always one at a time
    for f in range(len(X_test.values)):
        predict_list = []
        X_train_list = []
        x_val = X_test.values[f]
        
        predict_list.append(y_pred)
        final_list.append(y_pred)

        X_train_list.append(x_val)

        df_y = pd.DataFrame(predict_list,columns=['event'])
        df_x = pd.DataFrame(X_train_list)

        df = pd.DataFrame(X_train_list,columns=['event'])

        X_train = X_train.append(df)

        # update the training set
        y_train = y_train.append(df_y)

        count_min = Ecludian_min(x_val, y_pred, X_train, y_train, X_test)
        y_pred = float(y_train.values[count_min+1])
        # predict_list.append(y_pred)
        f = f + 1
        # print("f", f)
        count_min = 0

    return final_list



def Ecludian_min(init_x, init_y, X_train, y_train, X_test):
    # print(X_train.values)
    minValue = 1000000000
    count = len(y_train) - 1
    reverse_count = 0
    for x, y in zip(reversed(X_train.values[:-1]),reversed(y_train.values[:-1])):
        reverse_count = reverse_count + 1
        if (reverse_count%7 == 0):
            dist = math.sqrt(pow(float(x)-float(init_x),2) + pow(float(y)-float(init_y),2))
            if minValue > dist:
                minValue = dist
                mycount = count - reverse_count

    return mycount



Xtrain   = sys.argv[1].split(",") 
Ytrain   = sys.argv[2].split(",")
Xtest    = sys.argv[3].split(",")
Ypredict = replay(Xtrain, Xtest, Ytrain)
#Ypredict =Ypredict.tolist()
sep=""
for y in Ypredict:
        sys.stdout.write(sep);
        sys.stdout.write("%f" % y);
        sep = ","
sys.stdout.close()
