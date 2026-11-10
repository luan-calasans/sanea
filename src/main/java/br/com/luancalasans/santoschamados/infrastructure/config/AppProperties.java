package br.com.luancalasans.santoschamados.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name = "santos-chamados";
    private String env = "development";
    private String domain = "localhost";
    private String baseUrl = "http://localhost:8080";
    private String trustedProxies = "127.0.0.1";
    private final Atendimento atendimento = new Atendimento();
    private final Mail mail = new Mail();
    private final Recaptcha recaptcha = new Recaptcha();
    private final Attachment attachment = new Attachment();
    private final RateLimit rateLimit = new RateLimit();
    private final Login login = new Login();
    private final InitialUsers initialUsers = new InitialUsers();
    private final Cep cep = new Cep();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getTrustedProxies() {
        return trustedProxies;
    }

    public void setTrustedProxies(String trustedProxies) {
        this.trustedProxies = trustedProxies;
    }

    public Atendimento getAtendimento() {
        return atendimento;
    }

    public Mail getMail() {
        return mail;
    }

    public Recaptcha getRecaptcha() {
        return recaptcha;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public Login getLogin() {
        return login;
    }

    public InitialUsers getInitialUsers() {
        return initialUsers;
    }

    public Cep getCep() {
        return cep;
    }

    public boolean isProduction() {
        return "production".equalsIgnoreCase(env) || "prod".equalsIgnoreCase(env);
    }

    public static class Atendimento {
        private String cidade = "Santos";
        private String uf = "SP";

        public String getCidade() {
            return cidade;
        }

        public void setCidade(String cidade) {
            this.cidade = cidade;
        }

        public String getUf() {
            return uf;
        }

        public void setUf(String uf) {
            this.uf = uf;
        }
    }

    public static class Mail {
        private String from = "noreply@localhost";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }

    public static class Recaptcha {
        private boolean enabled = true;
        private String siteKey = "";
        private String secretKey = "";
        private String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSiteKey() {
            return siteKey;
        }

        public void setSiteKey(String siteKey) {
            this.siteKey = siteKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getVerifyUrl() {
            return verifyUrl;
        }

        public void setVerifyUrl(String verifyUrl) {
            this.verifyUrl = verifyUrl;
        }
    }

    public static class Attachment {
        private int maxFileSizeMb = 8;
        private int maxFiles = 3;
        private String storagePath = "./data/anexos";
        private int maxDimension = 1920;

        public int getMaxFileSizeMb() {
            return maxFileSizeMb;
        }

        public void setMaxFileSizeMb(int maxFileSizeMb) {
            this.maxFileSizeMb = maxFileSizeMb;
        }

        public int getMaxFiles() {
            return maxFiles;
        }

        public void setMaxFiles(int maxFiles) {
            this.maxFiles = maxFiles;
        }

        public String getStoragePath() {
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }

        public int getMaxDimension() {
            return maxDimension;
        }

        public void setMaxDimension(int maxDimension) {
            this.maxDimension = maxDimension;
        }

        public long maxFileSizeBytes() {
            return maxFileSizeMb * 1024L * 1024L;
        }
    }

    public static class RateLimit {
        private int publicProtocolLimit = 10;
        private int publicProtocolWindowSeconds = 60;
        private int publicProtocolBlockSeconds = 300;

        public int getPublicProtocolLimit() {
            return publicProtocolLimit;
        }

        public void setPublicProtocolLimit(int publicProtocolLimit) {
            this.publicProtocolLimit = publicProtocolLimit;
        }

        public int getPublicProtocolWindowSeconds() {
            return publicProtocolWindowSeconds;
        }

        public void setPublicProtocolWindowSeconds(int publicProtocolWindowSeconds) {
            this.publicProtocolWindowSeconds = publicProtocolWindowSeconds;
        }

        public int getPublicProtocolBlockSeconds() {
            return publicProtocolBlockSeconds;
        }

        public void setPublicProtocolBlockSeconds(int publicProtocolBlockSeconds) {
            this.publicProtocolBlockSeconds = publicProtocolBlockSeconds;
        }
    }

    public static class Login {
        private int maxAttempts = 5;
        private int lockMinutes = 5;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getLockMinutes() {
            return lockMinutes;
        }

        public void setLockMinutes(int lockMinutes) {
            this.lockMinutes = lockMinutes;
        }
    }

    public static class InitialUsers {
        private final InitialUser cidadao = new InitialUser();
        private final InitialUser sabesp = new InitialUser();
        private final InitialUser prefeitura = new InitialUser();
        private final InitialUser admin = new InitialUser();

        public InitialUser getCidadao() {
            return cidadao;
        }

        public InitialUser getSabesp() {
            return sabesp;
        }

        public InitialUser getPrefeitura() {
            return prefeitura;
        }

        public InitialUser getAdmin() {
            return admin;
        }
    }

    public static class InitialUser {
        private String email = "";
        private String password = "";
        private String nome = "";

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public boolean isConfigured() {
            return email != null && !email.isBlank()
                    && password != null && !password.isBlank()
                    && !password.startsWith("CHANGE_ME");
        }
    }

    public static class Cep {
        private int timeoutMs = 2500;
        private int cacheDays = 30;

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public int getCacheDays() {
            return cacheDays;
        }

        public void setCacheDays(int cacheDays) {
            this.cacheDays = cacheDays;
        }
    }
}
