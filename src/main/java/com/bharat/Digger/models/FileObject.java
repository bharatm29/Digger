package com.bharat.Digger.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileObject {
    private String name;
    private String url;
    private String download_url;
    private String type;
}
