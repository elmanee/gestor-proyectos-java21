$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Speech
$videoRoot=Join-Path $PSScriptRoot '../tmp/video'
$paragraphs=Get-Content (Join-Path $videoRoot 'narracion.json') -Raw -Encoding utf8 | ConvertFrom-Json
$speaker=New-Object System.Speech.Synthesis.SpeechSynthesizer
if($speaker.Voice.Culture.TwoLetterISOLanguageName -ne 'es'){throw 'Selecciona una voz de Windows en espanol antes de generar la narracion.'}
$speaker.Rate=0
for($n=0;$n -lt $paragraphs.Count;$n++){
    $speaker.SetOutputToWaveFile((Join-Path $videoRoot ('voz-'+($n+1)+'.wav')))
    $speaker.Speak($paragraphs[$n])
}
$speaker.Dispose()

