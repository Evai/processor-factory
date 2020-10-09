package com.heimdall.processor.core;

import com.squareup.javapoet.TypeName;

import java.util.List;


public class EnumElement {

    private String name;

    private List<Member> members;

    static class Member {
        private String name;
        private TypeName typeName;
        private Object value;

        public Member(String name, TypeName typeName, Object value) {
            this.name = name;
            this.typeName = typeName;
            this.value = value;
            if ("java.lang.String".equals(typeName.toString())) {
                String s = (String) value;
                s = s.replaceAll("\"","");
                this.value = s;
            }
        }

        public String getName() {
            return name;
        }

        public TypeName getTypeName() {
            return typeName;
        }

        public Object getValue() {
            return value;
        }
    }

    public EnumElement(String name, List<Member> members) {
        this.name = name;
        this.members = members;
    }

    public List<Member> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

}
