package com.timelineofwealth.service;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GeneratePassword {
    public static void main(String[] arg){

        System.out.println("YDhuri111 :"+ passwordEncoder().encode("YDhuri111"));
        //System.out.println("VPadave222 :"+ passwordEncoder().encode("VPadave222"));
        //System.out.println("Rohit :"+ passwordEncoder().encode("Rohit007"));
        //System.out.println("kkk123 :"+ passwordEncoder().encode("kkk123"));
        // System.out.println("vallabh.m :"+ passwordEncoder().encode("vallabh.m"));
        // System.out.println("Poonikh123 :"+ passwordEncoder().encode("Poonikh123"));
        //System.out.println("Mayank123 :"+ passwordEncoder().encode("Mayank123"));
        //System.out.println("Devang883 :"+ passwordEncoder().encode("Devang883"));

        /*
        Demo123 :$2a$10$C9Ez4CWi8s3DJ2c618gZfOHA064CDWNzHhLHQptfaH8.rIRlzNiHe
        Admin703 :$2a$10$yLrtIAznr3IwyyGv4Wj2yO2rm67gn3MchuK0Fs.UuIQdWowkemxiS
        Madhavi1985 :$2a$10$Nuz9Mn4SovtiHMwsRiqD3eW5DHncbQIjop2vBeJA9dKDdDdImuK6C
        Rohini0304 :$2a$10$ZDtcgbzTbieQdqX5E.0qDuob5G8cYmYokFxQVGHwXmT7OV9mqzs92
        Yatin218 :$2a$10$T2.zn.raKEGFqchMr.2zLusIa/V65CBxesC52cN7bMcrRmY/ca3me
        Yogesh1980 :$2a$10$z/Q8TVh.fAXUHoo9wwj.cO46bNeS.SWik0w.oFeUBsSywr3LS4SoK
        Pawar702 :$2a$10$G7HGj356utx0mpUczSKq9OJYeuFgS8XqTuyOJefe2EY0a32lbrcNC
        Rahul111 :$2a$10$D2zaA138F0IU1.Cf7j4kQO.mAROnXoANlVZRprbsjbQKNYCXSiKhy
        Prashant222 :$2a$10$abwcYXmedFI5yKVorc9n2O2IwxUeLz0s636nXnmdjxGkWDDFbztpW
        Sachin504 :$2a$10$lWnzSvQBPXvToLpWFPWDE.6TgA6/xTNBiadl.oKcDhtfSjVQbnISO
        Girish1110 :$2a$10$hO4NGE6qGX0XPz5XtVIbqeik9R4Si70VVA/or0NTIUGNfTXj9SVhy
        NishaAneeket123 :$2a$10$dmaIWfYNdmVSdJHgmYMuiOlCI4c55cV1Yujg5eAily9UHEqTq9HvS
        JayeshManu111 :$2a$10$bOyflnmzOFtmtqt9wGrYy.B6ZrKiWmOXQ9T0x4NxRVU9f.PzxKcyO
        Pagnis502 :$2a$10$5ufphFNQmDWNy45omuaj4e2/4OuKLsIounmLhhkbT8uEb4D4n9QXu
        Tuna1980 :$2a$10$prUa6b7WsfhN33u8khVx8.qrLNympG0bbmuqXbGHUpvPKcHOdTB.O
        Paresh111 :$2a$10$N0VLkKh9vm5uh8QsABFM.OEgTCKSGfIJp2HK1bgn.Ff/OdjVmH8p6
        SJP111 :$2a$10$uscbfv0eT8cMcHmdy6cf4OQM8lsSLUBCPW2JUkYvQXGJIQDZXiGU6
        ManMansi123 :$2a$10$.6cP9zzH3RgncPO3AX7E1uHoUhCzLo3AYXNekgXGlw28x9Wf7BZmW
        SharadSai111 :$2a$10$lZWRm6fh1dPdSbZ77ypZzOpXOBwD/7R02uVPE62.aLz21LmVTmsMe
        Amol231180 :$2a$10$UWYP.HPsGfVutk27fuHbLunHwEoa69dNmoBPQ4cxmnS0dpXmoO2.i
        Swapnil030291 :$2a$10$ao3jhOkCysUM2WpEUFyD8OOqnG8WeUGa6nKMEZ3sPXuB5GIxctIre
        Sud221278 :$2a$10$e7n.EvieSkZp6FusyXTbbezegzCoC5HbnKZMJZeBMsU4g2dRVZSga
        Jaidas111 :$2a$10$uIxSv6InOqGux36sRrmT2Os9NPmf6NZoF/jrF7NE6GA9UdKkQptaS
        Shekhar1979 :$2a$10$nMjB7sAj5bQOSzuANCJyZu0ScYIuxC75GVsfiKnd3VkIlc7Zfpjo.
        Surendra123 :$2a$10$1qtXg4TIYHAmrJ4HkJhVGO217ki5FvJDgX3GJ6xvdAumV7SqiipDy
        Meena111 :$2a$10$5yUjWIe5SGAX/24.X.xXoenfuaCT5WBfp9lCePA0loA06msHUw1Pa
        Uday123 :$2a$10$qhphlpplVA3JjuxJrnr2wOCJXjcqCr9WhR2Y2P2HVO92yyTDLE7Ry
        Mayank123 :$2a$10$u4pb/WR3qAG3KCHcfnJ.neH7o2SRV.2UAyn9PfgJq0FwSWmIAf8ca
        Devang883 :$2a$10$cw.lsbnBcVoSqpWUWoMJSeQ4Y.ikbqnBAfnc0rFLO5IFuU04vyk7O
        Poonikh123 :
         */

    }
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

