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
def replay(y_train, x_test):
    predict_list = []
    y_train = DataFrame(y_train,columns=['event'])
    init_y = float(y_train.values[-1])
    # print("my initial values", init_y)

    count_min = abs_min(init_y, y_train)
    # print("min", count_min)
    y_pred = float(y_train.values[count_min+1])
    predict_list.append(y_pred)

    count_min = count_min + 1
    for f in range(len(x_test)-1):
        # print("f", f)
        # if(count_min+1 <= len(y_train.values)-1):
        y_pred = float(y_train.values[count_min+1])
        count_min = count_min + 1

        predict_list.append(y_pred)
    
    return predict_list

def abs_min(init_y, y_train):
    minValue = 1000000000
    count = len(y_train) - 1
    mycount = 0
    reverse_count = 0


    for y in reversed(y_train.values[:-1]):
        reverse_count = reverse_count + 1
        if (reverse_count%28 == 0):
            dist = abs(float(y) - float(init_y))
            if minValue > dist:
                minValue = dist
                mycount = count - reverse_count
    # print(y_value)
    # print(mycount)
    return mycount

    
Xtrain   = sys.argv[1].split(",") 
Ytrain   = sys.argv[2].split(",")
Xtest    = sys.argv[3].split(",")
Ypredict = replay(Ytrain, Xtest)
#Ypredict =Ypredict.tolist()
sep=""
for y in Ypredict:
        sys.stdout.write(sep);
        sys.stdout.write("%f" % y);
        sep = ","
sys.stdout.close()