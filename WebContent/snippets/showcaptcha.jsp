<%@page 
import="net.tanesha.recaptcha.*"
import="com.freewinesearcher.common.Wijnzoeker"
%>

<%
     //Pass in the right public key and private key here.. 
     ReCaptcha captcha = 
       ReCaptchaFactory.
	newReCaptcha(Wijnzoeker.captchapublickey, Wijnzoeker.captchaprivatekey, false);
        String captchaScript = captcha.createRecaptchaHtml(request.getParameter("error"), null);
	out.print(captchaScript);
%>