package com.bharat.Digger.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirObject {
    private String name;
    private String path;
    private List<FileObject> entries;
}
