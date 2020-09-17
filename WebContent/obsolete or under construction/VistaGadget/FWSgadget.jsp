<html >
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=Unicode" />
        <title>Hello World</title>
        <style type="text/css">
        body
        {
            margin: 0;
            width: 130px;
            height: 75px;
            font-family: verdana;
            font-weight: bold;
            font-size: small;
        }
        #gadgetContent
        {
            margin-top: 20px;
            width: 130px;
            vertical-align: middle;
            text-align: center;
            overflow: hidden;
        }
        </style>
        <script type="text/jscript" language="jscript">
            // Initialize the gadget.
            function init()
            {
                var oBackground = document.getElementById("imgBackground");
                oBackground.src = "url(images/background.png)";
            }
        </script>
    </head>
	
    <body onload="init()">
        <g:background id="imgBackground">
            <span id="gadgetContent">Hello World!</span>
        </g:background>
    </body>
</html>