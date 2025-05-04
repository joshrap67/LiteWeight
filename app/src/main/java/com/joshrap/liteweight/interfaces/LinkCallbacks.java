package com.joshrap.liteweight.interfaces;

import com.joshrap.liteweight.models.user.Link;

public interface LinkCallbacks {

    void onClear(Link link, int index);

    void onClick(Link link, int index);
}
