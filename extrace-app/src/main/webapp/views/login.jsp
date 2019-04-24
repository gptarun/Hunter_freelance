<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Extrace</title>

</head>

<body>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <script type="text/javascript" src="https://www.instantor.com.br/frame-loader/instantor-0.7.3.min.js"></script>
    <h1>Please enter bank account info</h1>
    <br>
    Hunter :<input type="button" id="Save" onclick="callHunterAPI();" value="Connect with Hunter"></input><br><br>

    Testing :<input type="button" id="Query" onclick="queryMe();" value="Query Me"></input><br><br>


    <div id="itor">
        Instantor:<input type="button" id="load" value="Connect with Instantor"><br><br>
        <iframe width="400" height="150"></iframe>
    </div>
    <script>

        //<![CDATA[
        $("#load").click(function () {
            var itor = new Instantor('acordo-certo-74e83a42-c174-4d07-ba28-1c164b1fd3f6.br');
            itor.load('#itor');

        });     
        //]]>


        function callHunterAPI() {
            $.ajax({
                url: "/callHunter",
                type: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                success: function (data) {
                    window.location.href = data.sessionUrl;
                }, error: function (jqXHR, textStatus, errorThrown) {
                }
            });
        }

        function queryMe() {
            $.ajax({
                url: "/queryMe",
                type: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                success: function (data) {
                    console.log("URL: " + data.baseName);
                }, error: function (jqXHR, textStatus, errorThrown) {
                }
            });
        }

        function callInstantor() {
            $.ajax({
                url: "/callInstantor",
                type: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                success: function (data) {
                    console.log("Response : " + data);
                }, error: function (jqXHR, textStatus, errorThrown) {
                }
            });
        }
    </script>
</body>

</html>