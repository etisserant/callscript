# SSH Callback use case #

In order to reduces your expenses, you prefer to use minutes from your VoIP provider rather than those from your mobile phone operator. Using a soft phone (sipdroid, etc) over your data connection is not applicable (restricted connection, narrow bandwidth, high rate, VoIP not allowed in EULA, etc).

## Phone side ##
Hereafter is content of `/sdcard/callscript.sh`
```
echo `date` $1 >> /sdcard/calls
ssh -y -i /sdcard/id_dropbear my_user@my_home "screen -d -m python ~/call.py 0123456789 $1"
```
  * called phone numbers are logged in `/sdcard/calls`
  * uses cyanogen's included [dropbear ssh client](http://matt.ucc.asn.au/dropbear/dropbear.html)
  * remote python script is launched in a detached screen, returning immediately
  * `my_user`,  `my_home` and `0123456789` have to be substituted by your login, host and phone number

## Server side ##

Hereafter is content of `~/call.py`, for user `my_user` on `my_home` host :
```
import subprocess
import os
import sys
import time

log = open("call_log.txt",'at')

cmd="/path/to/my/pjsua"
args="\
--id sip:my_sip_user@my.sip.registrar \
--registrar sip:my.sip.registrar \
--realm '*' \
--username my_sip_user \
--password my_sip_password \
--auto-loop \
--null-audio \
--app-log-level=4 \
--stdout-refresh=5 \
--stdout-refresh-text=STDOUT_XXX \
--stdout-no-buf"

log.write("###################################################\n")
log.write("# CALL at "+time.ctime()+" : "+sys.argv[1]+" -> "+sys.argv[2]+"\n")
log.write("###################################################\n")

proc = subprocess.Popen( cmd + " " + args, shell=True, cwd=os.getcwd(), stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

outchunk = ''
while outchunk.find("SIP/2.0 200 OK") == -1:
    outchunk = proc.stdout.readline()
    log.write(outchunk)

proc.stdin.write("m\nsip:"+sys.argv[1]+"@my.sip.registrar\n")

log.write("################ Called")

outchunk = ''
while outchunk.find("SIP/2.0 200 OK") == -1:
    outchunk = proc.stdout.readline()
    log.write(outchunk)

proc.stdin.write("x\nsip:"+sys.argv[2]+"@my.sip.registrar\n")

log.write("################ Transfered")

outchunk = ''
while outchunk.find("SIP/2.0 200 OK") == -1:
    outchunk = proc.stdout.readline()
    log.write(outchunk)

proc.stdin.write("q\n")
proc.wait()

log.write("###################################################\n")
log.write("# END at "+time.ctime()+" : "+sys.argv[1]+" -> "+sys.argv[2]+"\n")
log.write("###################################################\n")
```

  * first, initiate a call towards caller,
  * then, transfer call towards callee and quit.
  * pjsua log is appended to call\_log.txt
  * `cmd="...` gives [pjsip](http://www.pjsip.org/) command line SIP user agent path.
  * `my_sip_user`, `my_sip_password`, `my.sip.registrar` are to be substituted by valid SIP account details