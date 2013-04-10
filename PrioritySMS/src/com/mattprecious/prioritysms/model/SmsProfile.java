
package com.mattprecious.prioritysms.model;

import java.util.HashSet;
import java.util.Set;

public class SmsProfile extends BaseProfile {
    private Set<String> keywords;

    public SmsProfile() {
        keywords = new HashSet<String>();
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public void addKeyword(String keyword) {
        keywords.add(keyword);
    }

    public void removeKeyword(String keyword) {
        keywords.remove(keyword);
    }
}
