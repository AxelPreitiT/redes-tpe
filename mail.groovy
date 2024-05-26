def sendEmail(development_link, abort_link, build_number, subject){
    emailext mimeType: 'text/html',
    subject: "${subject}",
    to: '${DEFAULT_RECIPIENTS}',
    body: """
    <div style="text-align: center;">
        <img src="https://testeandosoftware.com/wp-content/uploads/2015/01/jenkins_logo.png" alt="Jenkins. Servidor de integración continua gratuito - Testeando Software">
        <br>
        <span style="font-size: 18pt;">
            <strong>You have a new pending approval!</strong>
        </span>
        <br>
        <br>To abort or accept deployment #${build_number}, please click the "Approve/Abort" button below.
        <br>
        <br>If you prefer to review the build first, you can do so by clicking the "Development build" button.
        <br>
        <br>
        <br>
        <div>
            <a href="${abort_link}" target="_blank" class="cloudHQ__gmail_elements_final_btn" style="background-color: #d33834; color: #ffffff; border: 4px solid #000000; border-radius: 15px; box-sizing: border-box; font-size: 13px; font-weight: bold; line-height: 35px; padding: 6px 12px; text-align: center; text-decoration: none; text-transform: uppercase; vertical-align: middle;" rel="noopener">Approve/Abort</a>
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