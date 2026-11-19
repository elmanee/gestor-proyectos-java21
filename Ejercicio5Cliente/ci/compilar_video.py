from pathlib import Path
import subprocess,json
R=Path.cwd(); v=R/'tmp/video'; ff='ffmpeg'; probe='ffprobe'; durations=[]
for i in range(1,9):
 d=float(subprocess.check_output([probe,'-v','error','-show_entries','format=duration','-of','default=noprint_wrappers=1:nokey=1',str(v/f'voz-{i}.wav')],text=True).strip());durations.append(d)
 subprocess.run([ff,'-y','-loglevel','error','-loop','1','-framerate','10','-i',str(v/f'escena-{i}.png'),'-i',str(v/f'voz-{i}.wav'),'-c:v','libx264','-preset','ultrafast','-tune','stillimage','-pix_fmt','yuv420p','-r','10','-c:a','aac','-b:a','96k','-shortest',str(v/f'clip-{i}.mp4')],check=True)
(v/'lista.txt').write_text(''.join("file '"+str(v/f'clip-{i}.mp4').replace('\\','/')+"'\n" for i in range(1,9)),encoding='utf-8')
out=R/'entrega/Video-evidencia-Practica-3.mp4'
subprocess.run([ff,'-y','-loglevel','error','-f','concat','-safe','0','-i',str(v/'lista.txt'),'-c','copy','-movflags','+faststart',str(out)],check=True)
(v/'duraciones.json').write_text(json.dumps(durations))
print({'duracion_segundos':round(sum(durations),1),'bytes':out.stat().st_size})
