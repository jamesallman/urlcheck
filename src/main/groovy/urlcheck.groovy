import org.jsoup.Jsoup

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class urlcheck {
    def visited = []
    int depth = 0

    static void main(String[] args) {
        // Disable SSL certificate validation
        def nullTrustManager = [
                checkClientTrusted: { chain, authType -> },
                checkServerTrusted: { chain, authType -> },
                getAcceptedIssuers: { null }
        ]
        def nullHostnameVerifier = [
                verify: { hostname, session -> true }
        ]

        SSLContext sc = SSLContext.getInstance("SSL")
        sc.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)

        System.in.eachLine { url ->
            if (url.matches(/(?i)quit/)) { System.exit(0) }
            new urlcheck().check(url)
        }
    }

    void check(String url) {
        go(url)
        println ""
    }

    void redirect(String url) {
        print " [REDIR] -> "
        go(url)
    }

    void go(String url) {
        depth++
        print url

        if (url in visited || depth > 10) {
            print " [WTF?!]"
            return
        }

        visited << url

        try {
            new URL(url).openConnection().with {
                defaultUseCaches = false
                requestMethod = "GET"
                connect()
                print " [$responseCode]"
                switch(responseCode) {
                    // OK
                    case 200:
                        def doc = Jsoup.parse(inputStream, "UTF-8", url)
                        doc.select("meta[http-equiv=refresh]").each {
                            def matcher = it.attr("content") =~ /(?i)\d+;\s*URL=(.*)/
                            if (matcher.matches()) {
                                redirect(matcher.group(1))
                            } else if (doc.title() =~ /(?i)internet authentication/) {
                                print " [AUTH]"
                            }
                        }
                        break
                    // Redirection
                    case 300..399:
                        headerFields["Location"].each {
                            redirect(it)
                        }
                        break
                }
                this
            }
        } catch (UnknownHostException ignored) {
            print " [NXDOMAIN]"
        } catch (Exception ex) {
            print " [$ex.message]"
        }
        depth--
    }
}
