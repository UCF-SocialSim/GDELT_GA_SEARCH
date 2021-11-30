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
import numpy as np
from pandas import DataFrame
import pandas as pd
from sklearn import datasets, ensemble
def GradientBoosting(X_train, X_test, y_train):	
    # X_train = np.array(X_train)
    # y_train = np.array(y_train)
    # X_train=X_train.reshape(X_train)
    # y_train=y_train.reshape(y_train)
    X_train = DataFrame(X_train,columns=['event'])
    y_train = DataFrame(y_train,columns=['event'])
    X_test = DataFrame(X_test,columns=['event'])
    params = {'n_estimators': 500,
          'max_depth': 4,
          'min_samples_split': 5,
          'learning_rate': 0.01,
          'loss': 'ls'}
    # regressor = ElasticNet(random_state=0)
    regressor =ensemble.GradientBoostingRegressor(**params)
    regressor.fit(X_train, y_train)
    y_pred = regressor.predict(X_test)
    y_pred[y_pred<0]=0
    # df = pd.DataFrame({'Actual': y_test, 'Predicted': y_pred})
    # df = df.set_index(y_test.index.date)
    return y_pred
Xtrain   = sys.argv[1].split(",") 
Ytrain   = sys.argv[2].split(",")
Xtest    = sys.argv[3].split(",")
Ypredict = GradientBoosting(Xtrain, Xtest, Ytrain)
sep=""
for y in Ypredict:
    sys.stdout.write(sep);
    sys.stdout.write("%f" % y);
    sep = ","
sys.stdout.close()