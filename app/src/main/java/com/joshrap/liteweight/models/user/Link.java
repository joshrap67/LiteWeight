package com.joshrap.liteweight.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Link {

    private String url;
    private String label;

    public Link(Link link) {
        this.url = link.getUrl();
        this.label = link.getLabel();
    }
}
