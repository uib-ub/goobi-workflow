/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

public class AdditionalField {
    @Getter
    @Setter
    private String titel;
    @Getter
    private String wert = "";
    @Setter
    @Getter
    private boolean required = false;
    @Getter
    private String from = "process";
    @Getter
    @Setter
    private List<SelectItem> selectList;
    @Setter
    @Getter
    private boolean ughbinding = false;
    @Getter
    private String docstruct;
    @Getter
    @Setter
    private String metadata;
    @Getter
    private String isdoctype = "";
    @Getter
    private String isnotdoctype = "";
    @Getter
    private String initStart = ""; // defined in goobi_projects.xml
    @Getter
    private String initEnd = "";
    @Setter
    private boolean autogenerated = false;
    @Getter
    @Setter
    private boolean multiselect = false;

    @Getter
    @Setter
    private String fieldType;

    public void setInitStart(String newValue) {
        this.initStart = newValue;
        if (this.initStart == null) {
            this.initStart = "";
        }
        this.wert = this.initStart + this.wert;
    }

    public void setInitEnd(String newValue) {
        this.initEnd = newValue;
        if (this.initEnd == null) {
            this.initEnd = "";
        }
        this.wert = this.wert + this.initEnd;
    }

    public void setWert(String newValue) {
        if (newValue == null || newValue.equals(this.initStart)) {
            newValue = "";
        }
        if (newValue.startsWith(this.initStart)) {
            this.wert = newValue + this.initEnd;
        } else {
            this.wert = this.initStart + newValue + this.initEnd;
        }
    }

    public void setFrom(String infrom) {
        if (infrom != null && infrom.length() != 0) {
            this.from = infrom;
        }
    }

    public void setDocstruct(String docstruct) {
        this.docstruct = docstruct;
        if (this.docstruct == null) {
            this.docstruct = "topstruct";
        }
    }

    public void setIsdoctype(String isdoctype) {
        this.isdoctype = isdoctype;
        if (this.isdoctype == null) {
            this.isdoctype = "";
        }
    }

    public void setIsnotdoctype(String isnotdoctype) {
        this.isnotdoctype = isnotdoctype;
        if (this.isnotdoctype == null) {
            this.isnotdoctype = "";
        }
    }

    public boolean getShowDependingOnDoctype(String docType) {

        /* wenn nix angegeben wurde, dann anzeigen */
        if ("".equals(this.isdoctype) && "".equals(this.isnotdoctype)) {
            return true;
        }

        /* wenn pflicht angegeben wurde */
        if (!"".equals(this.isdoctype) && !StringUtils.containsIgnoreCase(isdoctype, docType)) {
            return false;
        }

        /* wenn nur "darf nicht" angegeben wurde */
        return "".equals(this.isnotdoctype) || !StringUtils.containsIgnoreCase(isnotdoctype, docType);
    }

    /**
     * @return the autogenerated
     */
    public boolean getAutogenerated() {
        return this.autogenerated;
    }

    public List<String> getValues() {
        if (wert != null && !wert.isEmpty()) {
            String[] parts = wert.split(";");
            return Arrays.asList(parts);
        } else {
            return new ArrayList<>();
        }
    }

    public void setValues(List<String> values) {
        StringBuilder bld = new StringBuilder();
        for (String part : values) {
            bld.append(part);
            bld.append(";");
        }
        wert = bld.toString();
        if (wert.endsWith(";")) {
            wert = wert.substring(0, wert.length() - 1);
        }
    }
}

/* =============================================================== */
