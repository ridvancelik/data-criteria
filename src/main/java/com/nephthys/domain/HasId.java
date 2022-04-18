package com.nephthys.domain;

import java.io.Serializable;

public interface HasId<I> extends Serializable {
    I getId();

    void setId(I id);
}
