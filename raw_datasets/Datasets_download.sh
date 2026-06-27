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



#The curl command that are not used are to limit the amount of download done each time when github performs the action.

#curl -L "https://drive.usercontent.google.com/download?id=1GjWrIZq_mx8Ugpxrm-_yzzbXzMhZ3FAk" -o 'raw_datasets/bus/lyontcl/Agency.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1EXi_fUcYtKaUYJA5NIuoTMbljo9Hq2YH" -o 'raw_datasets/bus/lyontcl/Calendar dates.txt'
curl -L "https://drive.usercontent.google.com/download?id=1y1JWkDTyFtSttHzhAsDBwhIqMWeYFHSY" -o 'raw_datasets/bus/lyontcl/trips.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1R-EA9Wr58JmG1to4r9OX8IP73Fi9BlA7" -o 'raw_datasets/bus/lyontcl/calendar.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1j0L2ZQIysFtpHXmDMVNxKvl5ksljOz_C" -o 'raw_datasets/bus/lyontcl/fare_attributes.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1X8zInfwPeClbLZO_AUmc6cIe-0vZuTzv" -o 'raw_datasets/bus/lyontcl/fare_rules.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1QGA8kJyxtTpX1DT091M4z8KlZDfA8y5s" -o 'raw_datasets/bus/lyontcl/feed_info.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1X9FnWRx7knyiztP-GKDUa_FMifSUN-3k" -o 'raw_datasets/bus/lyontcl/frequencies.txt'
#curl -L "https://drive.usercontent.google.com/download?id=105SVONlU-kcgqYUv50jlnYftTyzkS6vU" -o 'raw_datasets/bus/lyontcl/id_mappings.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1uFlgr0VIMATCc2n2K3p074WLCIpbFUDG" -o 'raw_datasets/bus/lyontcl/routes.txt'
#curl -L "https://drive.usercontent.google.com/download?id=14ZFanoyzm6VtNT6y6B0M8F_9HVY1ryDA&confirm" -o 'raw_datasets/bus/lyontcl/shapes.txt' #(/!\ TOO BIG CANNOT DIRECTLY DOWNLOAD)
curl -L "https://drive.usercontent.google.com/download?id=1MSZ9l3ur_IjNrx4iom7Af4dMLvhgbG7p&confirm" -o 'raw_datasets/bus/lyontcl/Stop_times.txt' #Too big to download normally so we add &confirm to bypass the confirmation screen
curl -L "https://drive.usercontent.google.com/download?id=1SWU4ytmigCsoPYn2juqbQ_d29NKATu19" -o 'raw_datasets/bus/lyontcl/stops.txt'
#curl -L "https://drive.usercontent.google.com/download?id=1Fy8Ip9c57B5M1SOwDNfe1l2EUmkd2Wwm" -o 'raw_datasets/bus/lyontcl/transfers.txt'


#Metro :
#1  https://drive.usercontent.google.com/download?id=15gYhnrVg9B7p1QEf143FMG0-8Ieb2ieb  = horaires_tcl.csv
#2  https://drive.usercontent.google.com/download?id=1Q8iHKB9dEe3Gkj3tUOcZZRuRaTfoLtIJ  = lignes-metro-funiculaire-reseau-transports-commun-lyonnais-v2
#3  https://drive.usercontent.google.com/download?id=17hCUhL46OkZ6Am4ykDPDuCzSqhw_r0y-  = stations-metro-reseau-transports-commun-lyonnais.csv

#The curl command that are not used are to limit the amount of download done each time when github performs the action.

curl -L "https://drive.usercontent.google.com/download?id=15gYhnrVg9B7p1QEf143FMG0-8Ieb2ieb" -o 'raw_datasets/metro/horaires_tcl.csv'
#curl -L "https://drive.usercontent.google.com/download?id=1Q8iHKB9dEe3Gkj3tUOcZZRuRaTfoLtIJ" -o 'raw_datasets/metro/lignes-metro-funiculaire-reseau-transports-commun-lyonnais.csv'
curl -L "https://drive.usercontent.google.com/download?id=17hCUhL46OkZ6Am4ykDPDuCzSqhw_r0y-" -o 'raw_datasets/metro/stations-metro-reseau-transports-commun-lyonnais.csv'




#More documentation : https://gist.github.com/tanaikech/f0f2d122e05bf5f971611258c22c110f