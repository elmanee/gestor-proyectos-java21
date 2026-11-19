const fs=require('fs');
const base='/var/jenkins_home/jobs'; const summary={};
for (const name of ['gestor-frontend','gestor-backend','gestor-database']) {
 const root=base+'/'+name+'/builds';
 const numbers=fs.readdirSync(root).filter(x=>/^\d+$/.test(x)).map(Number).sort((a,b)=>b-a);
 if (!numbers.length) {summary[name]={result:'NOT_BUILT'};continue;}
 const n=numbers[0];let xml='';try{xml=fs.readFileSync(root+'/'+n+'/build.xml','utf8')}catch{}
 let log='';try{log=fs.readFileSync(root+'/'+n+'/log','utf8')}catch{}
 const status=log.match(/^Finished: (SUCCESS|FAILURE|ABORTED|UNSTABLE|NOT_BUILT)\s*$/m)?.[1] || 'RUNNING';
 summary[name]={build:n,result:status,url:'http://localhost:8080/job/'+name+'/'+n+'/',duration:Number(xml.match(/<duration>(\d+)<\/duration>/)?.[1]||0),timestamp:Number(xml.match(/<startTime>(\d+)<\/startTime>/)?.[1]||0),gitCommit:log.match(/Checking out Revision ([a-f0-9]{40})/)?.[1],tests:log.match(/Tests run: (\d+), Failures: 0, Errors: 0, Skipped: 0/g)?.slice(-3)};
}
process.stdout.write(JSON.stringify(summary,null,2));
