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
from sklearn.linear_model import ElasticNet
import numpy as np
from pandas import DataFrame
import pandas as pd

def ElasticNet_(X_train, X_test, y_train):	
    X_train = DataFrame(X_train,columns=['event'])
    y_train = DataFrame(y_train,columns=['event'])
    X_test = DataFrame(X_test,columns=['event'])
    regressor = ElasticNet(random_state=0)
    regressor.fit(X_train, y_train)
    coeff_df = pd.DataFrame(regressor.coef_, X_train.columns, columns=['Coefficient'])
    y_pred = regressor.predict(X_test)
    y_pred[y_pred<0]=0
    return y_pred
Xtrain   = sys.argv[1].split(",") 
Ytrain   = sys.argv[2].split(",")
Xtest    = sys.argv[3].split(",")
Ypredict = ElasticNet_(Xtrain, Xtest, Ytrain)
Ypredict =Ypredict.tolist()
sep=""
for y in Ypredict:
        sys.stdout.write(sep);
        sys.stdout.write("%f" % y);
        sep = ","
sys.stdout.close()