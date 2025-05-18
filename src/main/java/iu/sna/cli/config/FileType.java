package iu.sna.cli.config;

public enum FileType {
    txt,
    md;

    public static FileType getFileType(String fileType) {
        try {
            return valueOf(fileType);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
