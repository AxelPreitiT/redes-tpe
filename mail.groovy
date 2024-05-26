def sendEmail(development_link, abort_link, build_number, subject, recipients){
    emailext mimeType: 'text/html',
    subject: "${subject}",
    to: '${recipients}',
    body: """
    <div style="text-align: center;">
        <img src="https://testeandosoftware.com/wp-content/uploads/2015/01/jenkins_logo.png" alt="Jenkins. Servidor de integración continua gratuito - Testeando Software">
        <br>
        <span style="font-size: 18pt;">
            <strong>¡Tienes una nueva aprobación pendiente!</strong>
        </span>
        <br>
        <br>Para abortar o aceptar el deployment #${build_number} por favor haz click en el boton "Aprobar/Abortar" debajo.
        <br>
        <br>Si previamente prefieres revisar el build puedes revisarlo mediante el boton "Development build".
        <br>
        <br>
        <br>
        <div>
            <a href="${abort_link}" target="_blank" class="cloudHQ__gmail_elements_final_btn" style="background-color: #d33834; color: #ffffff; border: 4px solid #000000; border-radius: 15px; box-sizing: border-box; font-size: 13px; font-weight: bold; line-height: 35px; padding: 6px 12px; text-align: center; text-decoration: none; text-transform: uppercase; vertical-align: middle;" rel="noopener">Aprobar/Abortar</a>
            <a href="${development_link}" target="_blank" class="cloudHQ__gmail_elements_final_btn" style="background-color: #d33834; color: #ffffff; border: 4px solid #000000; border-radius: 15px; box-sizing: border-box; font-size: 13px; font-weight: bold; line-height: 35px; padding: 6px 12px; text-align: center; text-decoration: none; text-transform: uppercase; vertical-align: middle;" rel="noopener">Development build</a>
        </div>
        <br>
        <br>
        <br>
        <br>
    </div>
    """
}

return this