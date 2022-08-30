package com.github.DiachenkoMD.web.utils;

/**
 * Simple utility for formatting emails. Inside contains email template and simply inserts provided at {@link #format(String, String)} data.
 */
public class EmailFormatter {

    /**
     * Method for inserting specified data inside email template.
     * @param title
     * @param body
     * @return html form of email
     */
    public static String format(String title, String body){
        return "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\"\n" +
                "          content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                "\n" +
                "    <style>\n" +
                "        @import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@100;300;400;500;600;700&display=swap');\n" +
                "\n" +
                "        *{\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        body {\n" +
                "            background-color: rgb(28, 28, 30);\n" +
                "            font-family: 'Montserrat', 'Segoe UI', sans-serif;\n" +
                "            -webkit-font-smoothing: antialiased;\n" +
                "            font-size: 14px;\n" +
                "            line-height: 1.4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            -ms-text-size-adjust: 100%;\n" +
                "            -webkit-text-size-adjust: 100%;\n" +
                "            color: rgb(174, 174, 178);\n" +
                "        }\n" +
                "        .container {\n" +
                "            margin: 20px auto;\n" +
                "            max-width: 580px;\n" +
                "            padding: 1.5rem;\n" +
                "            width: 100%;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "\n" +
                "        .content{\n" +
                "            background: rgb(36, 36, 38);\n" +
                "            padding: 1.5rem;\n" +
                "            border: 1px solid #353538;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "\n" +
                "        .logo{\n" +
                "            text-align: center;\n" +
                "            transform: scale(.7);\n" +
                "        }\n" +
                "\n" +
                "        .email-title{\n" +
                "            font-weight: normal;\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        .email-content a {\n" +
                "            display: inline-block;\n" +
                "            position: relative;\n" +
                "            color: #0087ca;\n" +
                "            padding: 5px 0;\n" +
                "            text-decoration: none;\n" +
                "            padding-bottom: 8px;\n" +
                "        }\n" +
                "        .email-content a::after {\n" +
                "            content: \"\";\n" +
                "            position: absolute;\n" +
                "            width: 100%;\n" +
                "            -webkit-transform: scaleX(0);\n" +
                "            transform: scaleX(0);\n" +
                "            height: 2px;\n" +
                "            bottom: 8px;\n" +
                "            left: 0;\n" +
                "            background-color: #0087ca;\n" +
                "            -webkit-transform-origin: bottom right;\n" +
                "            transform-origin: bottom right;\n" +
                "            -webkit-transition: -webkit-transform 0.25s ease-out;\n" +
                "            transition: -webkit-transform 0.25s ease-out;\n" +
                "            transition: transform 0.25s ease-out;\n" +
                "            transition: transform 0.25s ease-out, -webkit-transform 0.25s ease-out;\n" +
                "        }\n" +
                "        .email-content a:hover {\n" +
                "            color: #0087ca;\n" +
                "        }\n" +
                "        .email-content a:hover::after {\n" +
                "            -webkit-transform: scaleX(1);\n" +
                "            transform: scaleX(1);\n" +
                "            -webkit-transform-origin: bottom left;\n" +
                "        }\n" +
                "\n" +
                "        .micro-caution {\n" +
                "            font-size: 12px;\n" +
                "            color: var(--systemGray_accessible);\n" +
                "            opacity: 0.7;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                    "<div class=\"content\">\n" +
                    "    <div class=\"logo\">\n" +
                    "        <svg width=\"108\" height=\"27\" viewBox=\"0 0 108 27\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                    "            <path d=\"M14.048 26.432C12.104 26.432 10.292 26.12 8.612 25.496C6.956 24.848 5.516 23.936 4.292 22.76C3.068 21.584 2.108 20.204 1.412 18.62C0.74 17.036 0.404 15.296 0.404 13.4C0.404 11.504 0.74 9.764 1.412 8.18C2.108 6.596 3.068 5.216 4.292 4.04C5.54 2.864 6.992 1.964 8.648 1.34C10.304 0.691999 12.116 0.368 14.084 0.368C16.268 0.368 18.236 0.752 19.988 1.52C21.764 2.264 23.252 3.368 24.452 4.832L20.708 8.288C19.844 7.304 18.884 6.572 17.828 6.092C16.772 5.588 15.62 5.336 14.372 5.336C13.196 5.336 12.116 5.528 11.132 5.912C10.148 6.296 9.296 6.848 8.576 7.568C7.856 8.288 7.292 9.14 6.884 10.124C6.5 11.108 6.308 12.2 6.308 13.4C6.308 14.6 6.5 15.692 6.884 16.676C7.292 17.66 7.856 18.512 8.576 19.232C9.296 19.952 10.148 20.504 11.132 20.888C12.116 21.272 13.196 21.464 14.372 21.464C15.62 21.464 16.772 21.224 17.828 20.744C18.884 20.24 19.844 19.484 20.708 18.476L24.452 21.932C23.252 23.396 21.764 24.512 19.988 25.28C18.236 26.048 16.256 26.432 14.048 26.432ZM28.3903 26V0.799998H39.2983C41.5543 0.799998 43.4983 1.172 45.1303 1.916C46.7623 2.636 48.0223 3.68 48.9103 5.048C49.7983 6.416 50.2423 8.048 50.2423 9.944C50.2423 11.816 49.7983 13.436 48.9103 14.804C48.0223 16.148 46.7623 17.18 45.1303 17.9C43.4983 18.62 41.5543 18.98 39.2983 18.98H31.6303L34.2223 16.424V26H28.3903ZM44.4103 26L38.1103 16.856H44.3383L50.7103 26H44.4103ZM34.2223 17.072L31.6303 14.336H38.9743C40.7743 14.336 42.1183 13.952 43.0063 13.184C43.8943 12.392 44.3383 11.312 44.3383 9.944C44.3383 8.552 43.8943 7.472 43.0063 6.704C42.1183 5.936 40.7743 5.552 38.9743 5.552H31.6303L34.2223 2.78V17.072ZM54.863 26V0.799998H65.771C68.027 0.799998 69.971 1.172 71.603 1.916C73.235 2.636 74.495 3.68 75.383 5.048C76.271 6.416 76.715 8.048 76.715 9.944C76.715 11.816 76.271 13.436 75.383 14.804C74.495 16.148 73.235 17.18 71.603 17.9C69.971 18.62 68.027 18.98 65.771 18.98H58.103L60.695 16.424V26H54.863ZM70.883 26L64.583 16.856H70.811L77.183 26H70.883ZM60.695 17.072L58.103 14.336H65.447C67.247 14.336 68.591 13.952 69.479 13.184C70.367 12.392 70.811 11.312 70.811 9.944C70.811 8.552 70.367 7.472 69.479 6.704C68.591 5.936 67.247 5.552 65.447 5.552H58.103L60.695 2.78V17.072ZM86.2041 26V5.552H78.1401V0.799998H100.1V5.552H92.0361V26H86.2041ZM103.771 26.288C102.811 26.288 101.983 25.964 101.287 25.316C100.615 24.644 100.279 23.792 100.279 22.76C100.279 21.728 100.615 20.9 101.287 20.276C101.983 19.628 102.811 19.304 103.771 19.304C104.755 19.304 105.583 19.628 106.255 20.276C106.927 20.9 107.263 21.728 107.263 22.76C107.263 23.792 106.927 24.644 106.255 25.316C105.583 25.964 104.755 26.288 103.771 26.288Z\" fill=\"white\"/>\n" +
                    "        </svg>\n" +
                    "    </div>\n" +
                    "    <div class=\"email-content\">\n" +
                    "        <h2 class=\"email-title\">"+title+"</h2>\n" +
                    "        <div class=\"email-description\">\n" +
                    body +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    <div class=\"micro-caution\">2022 crrt.com</div>\n" +
                    "</div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}
