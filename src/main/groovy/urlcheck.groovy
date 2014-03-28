import org.jsoup.Jsoup

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class urlcheck {
    def visited = []

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

        def urlcheck = new urlcheck()

        System.in.eachLine { url ->
            if (url == "quit") { System.exit(0) }
            urlcheck.go(url)
        }
    }

    void go(String url) {
        visited = []
        go(url, 1)
        println ""
    }

    void go(String url, int depth) {
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
                if (responseCode in (300..399)) {
                    headerFields["Location"].each {
                        print " [REDIR] -> "
                        go(it, depth + 1)
                    }
                } else if (responseCode == 200) {
                    def doc = Jsoup.parse(inputStream, "UTF-8", url as String)
                    doc.select("meta[http-equiv=refresh]").each {
                        def matcher = it.attr("content") =~ /(?i)\d+;\s*URL=(.*)/
                        if (matcher.matches()) {
                            print " [REDIR] -> "
                            go(matcher.group(1), depth + 1)
                        } else if (doc.title() =~ /(?i)internet authentication/) {
                            print " [AUTH]"
                        }
                    }
                }
            }
        } catch (UnknownHostException ignored) {
            print " [NXDOMAIN]"
        } catch (Exception ex) {
            print " [$ex.message]"
        }
    }
}
