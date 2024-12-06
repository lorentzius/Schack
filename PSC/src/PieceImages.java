// This class is no longer used

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.security.cert.X509Certificate;

// No longer used.


public class PieceImages {
    BufferedImage BB, BK, BN, BP, BQ, BR, WB, WK, WN, WP, WQ, WR;
    PieceImages () {
        // first try from my website. Ugly stuff to get around certificates!
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }};

        //originals are here: String url = "https://upload.wikimedia.org/wikipedia/commons/2/28/Chess_nlt60.png";

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            BB = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/BB.png"));
            BK = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/BK.png"));
            BN = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/BN.png"));
            BP = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/BP.png"));
            BQ = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/BQ.png"));
            BR = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/BR.png"));
            WB = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/WB.png"));
            WK = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/WK.png"));
            WN = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/WN.png"));
            WP = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/WP.png"));
            WQ = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/WQ.png"));
            WR = ImageIO.read(new URL("https://user.it.uu.se/~joachim/Pieces/WR.png"));

        } catch (Exception e) {
            System.out.println("Failed to load image");
            System.out.println(e.getMessage());
        }
    }

    BufferedImage getBlackImage(PieceType pt) {
        switch (pt) {
            case BISHOP:
                return BB;
            case KING:
                return BK;
            case KNIGHT:
                return BN;
            case PAWN:
                return BP;
            case QUEEN:
                return BQ;
            case ROOK:
                return BR;
        };
        return null;
    }

    BufferedImage getWhiteImage(PieceType pt){
        switch (pt) {
            case BISHOP:
                return WB;
            case KING:
                return WK;
            case KNIGHT:
                return WN;
            case PAWN:
                return WP;
            case QUEEN:
                return WQ;
            case ROOK:
                return WR;
        };
        return null;    // ow java complains there is no return statement
    }

    BufferedImage getImage(PieceType pt, PieceColor pc) {
        if (pc == PieceColor.White) return getWhiteImage(pt);
        else return getBlackImage(pt);
    }
}
