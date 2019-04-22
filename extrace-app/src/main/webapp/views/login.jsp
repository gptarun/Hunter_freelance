<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Extrace</title>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
    <script type="text/javascript" src="https://www.instantor.com.br/frame-loader/instantor-0.7.3.min.js"></script>
</head>

<body>
    <h1>Please enter bank account info</h1>
    <br>
    Hunter :<input type="button" id="Save" onclick="callHunterAPI();" value="Connect with Hunter"></input><br><br>

    Testing :<input type="button" id="Query" onclick="queryMe();" value="Query Me"></input><br><br>
    Instantor: <input type="button" id="load" value="Connect with Instantor"></input><br><br>
    <iframe id="itor" width="400" height="150"></iframe>

    <script>

        //<![CDATA[
        $(function () {
            try {

                /* Enter API name Instantor has provided to you */
                var itor = new Instantor('company.inc');

                /* Optionally, enter relevant process details */

                /* Parameter 'environment' is used to controll endpoint for delivering data. Default is 'prod'. */
                itor.userParam('environment', 'test');

                /* 'uniqueID' is an example of parameter that could be used to link specific request made by an end user to a set of data provided by Instantor. */
                /* Parameter and value are returned in the reports. */
                itor.userParam('uniqueID', '69017b4d-6869-48d5-a029-f21042a69e35');

                /* Initiate the Instantor iframe at the targeted DOM element */
                itor.load('#itor');

                /* Optional function to process the feedback messages */
                itor.listener(function (response) {
                    switch (response) {
                        case 'process-finished':
                            /* Process finished successfully. */
                            break;
                        case 'process-error':
                            /* Process encountered an error. */
                            break;
                        case 'invalid-login':
                            /* User did not provide correct login credentials. */
                            break;
                        case 'too-many-retries':
                            /* User failed to login too many times, and should not repeat the process
                               again for 24 hours in order to prevent a net-banking lock. */
                            break;
                        default:
                            /* Process encountered an error. */
                            break;
                    }
                });
            } catch (err) { }
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