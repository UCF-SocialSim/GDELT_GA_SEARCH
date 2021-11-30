#
# GDELT_GA_SEARCH: GDELT Genetic Algorithm Search Tool
#   
# Copyright (C) 2021  John T. Murphy
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# This code was authored by John T. Murphy with contributions from Awrad Emad
# Harleen Lappano, and Lindsey Andrade
# 
# If you use this code or the tool in your work, please cite using the following bibtex:
# @book{murphyAndWadsworth2021,
#   author =       {Murphy, John T., and Wadsworth, Marin},
#   title =        {GDELT GA Search Users Manual},
#   year =         {2021},
#   url =          {http://USER_MANUAL_URL}
# }

suppressMessages(suppressWarnings(library(ggplot2)))
library(tidyr)
library(reshape2)
library(stringr)
library(hydroGOF)



# To prepare files for reading, take the console out of a GDELT search run and perform something like this:
# cat console_out.txt | grep PRED | tail -n 29 >~/Desktop/GDELT_Search/test.csv

# This case is for a 28-day prediction; (the '29' is used to get the header) 
# This is for the final prediction. If you want to get one of the training periods,
# you can use the name of the training period, e.g. TEST_2020.05.21
# as the 'grep' instead of the 'PRED' keyword

inputFileName="~/Desktop/GDELT_Search/test.csv"

dfFULL=read.table(inputFileName, header=TRUE,sep=",",na.strings="None", stringsAsFactors = FALSE)

dfFULL$ConvertedDate=as.POSIXct(dfFULL$Date, format="%Y-%m-%d")

dfFULL$Date=NULL
# Remove the first two columns
dfFULL = dfFULL[-1]
dfFULL = dfFULL[-1]

dfReshaped = melt(dfFULL, variable.name="iteration", value.name="value", id.vars=c("ConvertedDate"))

dfReshaped$iteration <- as.character(dfReshaped$iteration)
dfReshaped$iteration <- str_replace(dfReshaped$iteration, "GT", "0")
dfReshaped$iteration <- str_replace(dfReshaped$iteration, "Iteration_", "")
dfReshaped$iter = as.numeric(dfReshaped$iteration)
dfReshaped$A = sqrt(dfReshaped$iter)

GTOnly = dfReshaped[dfReshaped$iter == 0, ]
Search = dfReshaped[dfReshaped$iter != 0, ]
MaxOnly = dfReshaped[dfReshaped$iter == max(dfReshaped$iter), ]

mult = max(GTOnly$value) /  max(MaxOnly$value)



# Analysis
# Graph of NRMSE at each iteration; this shows how much each iteration gains (or losses)
dfMatrixB = dfFULL
dfMatrixB$GT = NULL
dfMatrixB$ConvertedDate = NULL
matrixB = data.matrix(dfMatrixB)
dfMatrixA = dfFULL$GT
matrixA = data.matrix(dfMatrixA)
matrixA = matrixA[, rep(1, each=ncol(matrixB))]

matrixA = matrixA[1:7,]
matrixB = matrixB[1:7,]
result =data.frame(nrmse(matrixB, matrixA, norm="sd"))
colnames(result)[1] = "nrmse"
result$index=c(1:ncol(matrixB))

plotToWrite<-ggplot() +
  geom_line(data=result, aes(x=index, y=nrmse))
print(plotToWrite)




plotToWrite<-ggplot() +  
  geom_line(data=GTOnly, aes(x=ConvertedDate, y=value, group=iteration), color="darkgreen", size=2) +
  geom_line(data=Search, aes(x=ConvertedDate, y=value, group=iteration, color=iter)) + scale_colour_gradient(low="gray", high="blue") +
  geom_line(data=MaxOnly, aes(x=ConvertedDate, y=value, group=iteration), color="orange", size=2) #+
# geom_line(data=MaxOnly, aes(x=ConvertedDate, y=value * mult, group=iteration), color="red", size=1)
print(plotToWrite)
