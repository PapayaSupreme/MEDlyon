#Sh file for the workflow to automatically download the datasets and run the Application.



#Bus : 
#1   https://drive.usercontent.google.com/download?export=download&id=1GjWrIZq_mx8Ugpxrm-_yzzbXzMhZ3FAk = Agency.txt
#2   https://drive.usercontent.google.com/download?export=download&id=1EXi_fUcYtKaUYJA5NIuoTMbljo9Hq2YH = Calendar dates.txt
#3   https://drive.usercontent.google.com/download?export=download&id=1y1JWkDTyFtSttHzhAsDBwhIqMWeYFHSY = trips.txt
#4   https://drive.usercontent.google.com/download?export=download&id=1R-EA9Wr58JmG1to4r9OX8IP73Fi9BlA7 = calendar.txt
#5   https://drive.usercontent.google.com/download?export=download&id=1j0L2ZQIysFtpHXmDMVNxKvl5ksljOz_C = fare_attributes.txt
#6   https://drive.usercontent.google.com/download?export=download&id=1X8zInfwPeClbLZO_AUmc6cIe-0vZuTzv = fare_rules.txt
#7   https://drive.usercontent.google.com/download?export=download&id=1QGA8kJyxtTpX1DT091M4z8KlZDfA8y5s = feed_info.txt
#8   https://drive.usercontent.google.com/download?export=download&id=1X9FnWRx7knyiztP-GKDUa_FMifSUN-3k = frequencies.txt
#9   https://drive.usercontent.google.com/download?export=download&id=105SVONlU-kcgqYUv50jlnYftTyzkS6vU = id_mappings.txt
#10  https://drive.usercontent.google.com/download?export=download&id=1uFlgr0VIMATCc2n2K3p074WLCIpbFUDG = routes.txt
#11  https://drive.usercontent.google.com/download?export=download&id=14ZFanoyzm6VtNT6y6B0M8F_9HVY1ryDA = shapes.txt (/!\ TOO BIG CANNOT DIRECTLY DOWNLOAD)
#12  https://drive.usercontent.google.com/download?export=download&id=1MSZ9l3ur_IjNrx4iom7Af4dMLvhgbG7p = Stop_times.txt (/!\ TOO BIG TO DOWNLOAD DIRECTLY)
#13  https://drive.usercontent.google.com/download?export=download&id=1SWU4ytmigCsoPYn2juqbQ_d29NKATu19 = stops.txt
#14  https://drive.usercontent.google.com/download?export=download&id=1Fy8Ip9c57B5M1SOwDNfe1l2EUmkd2Wwm = transfers.txt

#REMEMBER TO ADD --create-dirs option to the first curl command every-time!

#The curl command that are not used are to limit the amount of download done each time when github performs the action.

#curl -o "raw_datasets/bus/lyon_tcl/Agency.txt" -L "https://drive.usercontent.google.com/download?id=1GjWrIZq_mx8Ugpxrm-_yzzbXzMhZ3FAk" --create-dirs
#curl -o "raw_datasets/bus/lyon_tcl/Calendar dates.txt" -L "https://drive.usercontent.google.com/download?id=1EXi_fUcYtKaUYJA5NIuoTMbljo9Hq2YH" 
#'curl -o "raw_datasets/bus/lyon_tcl/trips.txt" -L "https://drive.usercontent.google.com/download?id=1y1JWkDTyFtSttHzhAsDBwhIqMWeYFHSY" --create-dirs
#curl -o "raw_datasets/bus/lyon_tcl/calendar.txt" -L "https://drive.usercontent.google.com/download?id=1R-EA9Wr58JmG1to4r9OX8IP73Fi9BlA7" 
#curl -o "raw_datasets/bus/lyon_tcl/fare_attributes.txt" -L "https://drive.usercontent.google.com/download?id=1j0L2ZQIysFtpHXmDMVNxKvl5ksljOz_C" 
#curl -o "raw_datasets/bus/lyon_tcl/fare_rules.txt" -L "https://drive.usercontent.google.com/download?id=1X8zInfwPeClbLZO_AUmc6cIe-0vZuTzv" 
#curl -o "raw_datasets/bus/lyon_tcl/feed_info.txt" -L "https://drive.usercontent.google.com/download?id=1QGA8kJyxtTpX1DT091M4z8KlZDfA8y5s" 
#curl -o "raw_datasets/bus/lyon_tcl/frequencies.txt" -L "https://drive.usercontent.google.com/download?id=1X9FnWRx7knyiztP-GKDUa_FMifSUN-3k" 
#curl -o "raw_datasets/bus/lyon_tcl/id_mappings.txt" -L "https://drive.usercontent.google.com/download?id=105SVONlU-kcgqYUv50jlnYftTyzkS6vU" 
#curl -o "raw_datasets/bus/lyon_tcl/routes.txt" -L "https://drive.usercontent.google.com/download?id=1uFlgr0VIMATCc2n2K3p074WLCIpbFUDG" 
#curl -o "raw_datasets/bus/lyon_tcl/shapes.txt" -L "https://drive.usercontent.google.com/download?id=14ZFanoyzm6VtNT6y6B0M8F_9HVY1ryDA&confirm" #(/!\ TOO BIG CANNOT DIRECTLY DOWNLOAD)
#'curl -o "raw_datasets/bus/lyon_tcl/Stop_times.txt" -L "https://drive.usercontent.google.com/download?id=1MSZ9l3ur_IjNrx4iom7Af4dMLvhgbG7p&confirm" #Too big to download normally so we add &confirm to bypass the confirmation screen
#'curl -o "raw_datasets/bus/lyon_tcl/stops.txt" -L "https://drive.usercontent.google.com/download?id=1SWU4ytmigCsoPYn2juqbQ_d29NKATu19" 
#curl -o "raw_datasets/bus/lyon_tcl/transfers.txt" -L "https://drive.usercontent.google.com/download?id=1Fy8Ip9c57B5M1SOwDNfe1l2EUmkd2Wwm" 


#Metro :
#1  https://drive.usercontent.google.com/download?id=15gYhnrVg9B7p1QEf143FMG0-8Ieb2ieb  = horaires_tcl.csv
#2  https://drive.usercontent.google.com/download?id=1Q8iHKB9dEe3Gkj3tUOcZZRuRaTfoLtIJ  = lignes-metro-funiculaire-reseau-transports-commun-lyonnais-v2
#3  https://drive.usercontent.google.com/download?id=17hCUhL46OkZ6Am4ykDPDuCzSqhw_r0y-  = stations-metro-reseau-transports-commun-lyonnais.csv

#The curl command that are not used are to limit the amount of download done each time when github performs the action.

curl -fsSL "https://drive.usercontent.google.com/download?id=15gYhnrVg9B7p1QEf143FMG0-8Ieb2ieb" --create-dirs > "raw_datasets/metro/horaires_tcl.csv" 
#curl -fsSL "https://drive.usercontent.google.com/download?id=1Q8iHKB9dEe3Gkj3tUOcZZRuRaTfoLtIJ" > "raw_datasets/metro/lignes-metro-funiculaire-reseau-transports-commun-lyonnais.csv" 
curl -fsSL "https://drive.usercontent.google.com/download?id=17hCUhL46OkZ6Am4ykDPDuCzSqhw_r0y-" > "raw_datasets/metro/stations-metro-reseau-transports-commun-lyonnais.csv"



#More documentation : https://gist.github.com/tanaikech/f0f2d122e05bf5f971611258c22c110f