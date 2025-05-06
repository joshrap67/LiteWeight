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

    public static boolean linksEqual(Link link1, Link link2) {
        return link1.getUrl().equals(link2.getUrl()) && link1.getLabel().equals(link2.getLabel());
    }
}
