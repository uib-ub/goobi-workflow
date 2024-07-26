/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * <p>
 * Visit the websites for more information.
 * - https://goobi.io
 * - https://www.intranda.com
 * - https://github.com/intranda/goobi-workflow
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.managedbeans;

import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.APIException;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.hateoas.HATEOASPaginator;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import io.goobi.workflow.api.vocabulary.helper.HierarchicalRecordComparator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.deltaspike.core.api.scope.WindowScoped;

import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Named
@WindowScoped
@Log4j2
public class VocabularyRecordsBean implements Serializable {
    private static final long serialVersionUID = 5672948572345L;

    private static final String RETURN_PAGE_OVERVIEW = "vocabulary_records";
    private static final String RETURN_PAGE_UPLOAD = "vocabulary_upload";

    private static final VocabularyAPIManager api = VocabularyAPIManager.getInstance();

    @Getter
    private transient HATEOASPaginator<VocabularyRecord, ExtendedVocabularyRecord, VocabularyRecordPageResult> paginator;

    @Getter
    private transient ExtendedVocabulary vocabulary;

    private transient VocabularySchema schema;

    @Getter
    private transient ExtendedVocabularyRecord currentRecord;

    @Getter
    private transient List<FieldDefinition> titleFields;

    private transient HierarchicalRecordComparator comparator;

    @Getter
    @Setter
    private Part uploadedFile;

    @Getter
    @Setter
    private boolean clearBeforeImport;

    public String load(ExtendedVocabulary vocabulary) {
        this.vocabulary = vocabulary;

        comparator = new HierarchicalRecordComparator();
        loadSchema();
        loadPaginator();
        loadFirstRecord();

        return RETURN_PAGE_OVERVIEW;
    }

    public void reload() {
        paginator.reload();
        loadFirstRecord();
    }

    public void edit(VocabularyRecord record) {
        this.currentRecord = new ExtendedVocabularyRecord(record);
        if (record.getParentId() != null) {
            findLoadedRecord(record.getParentId()).ifPresent(this::expandRecord);
        }
        expandParents(record);
    }

    public void createEmpty(Long parent) {
        VocabularyRecord record = new VocabularyRecord();
        record.setVocabularyId(vocabulary.getId());
        record.setParentId(parent);
        record.setFields(new HashSet<>());
        this.currentRecord = new ExtendedVocabularyRecord(record);
    }

    public void deleteRecord(VocabularyRecord rec) {
        try {
            api.vocabularyRecords().delete(rec);
            paginator.reload();
            loadFirstRecord();
        } catch (APIException e) {
            Helper.setFehlerMeldung(e);
        }
    }

    public void saveRecord(VocabularyRecord rec) {
        // TODO: Maybe replace current record
        try {
            VocabularyRecord newRecord = api.vocabularyRecords().save(rec);
            paginator.reload();
            loadChild(
                    newRecord.getId()
            );
            findLoadedRecord(newRecord.getId()).ifPresent(this::edit);
        } catch (APIException e) {
            Helper.setFehlerMeldung(e);
        }
    }

    public String uploadRecords() {
        return RETURN_PAGE_UPLOAD;
    }

    public String importRecords() {
        if (uploadedFile == null) {
            return "";
        }
        System.err.println(uploadedFile);
        String fileExtension = uploadedFile.getSubmittedFileName().substring(uploadedFile.getSubmittedFileName().lastIndexOf("."));
        switch (fileExtension) {
            case ".csv":
                if (clearBeforeImport) {
                    api.vocabularies().cleanImportCsv(this.vocabulary.getId(), uploadedFile);
                } else {
                    api.vocabularies().importCsv(this.vocabulary.getId(), uploadedFile);
                }
                break;
            case ".xlsx":
                if (clearBeforeImport) {
                    api.vocabularies().cleanImportExcel(this.vocabulary.getId(), uploadedFile);
                } else {
                    api.vocabularies().importExcel(this.vocabulary.getId(), uploadedFile);
                }
                break;
            default:
                throw new IllegalArgumentException("Unrecognized file type: \"" + fileExtension + "\"");
        }

        return load(this.vocabulary);
    }

    public void expandRecord(VocabularyRecord record) {
        for (long childId : record.getChildren()) {
            VocabularyRecord child = paginator.getItems().stream()
                    .filter(r -> r.getId() == childId)
                    .findFirst()
                    .orElseGet(() -> loadChild(childId));
//            child.setShown(true);
        }
//        record.setExpanded(true);
    }

    public boolean isHierarchical() {
        return Boolean.TRUE.equals(this.schema.getHierarchicalRecords());
    }

    public boolean isRootRecordCreationPossible() {
        return Boolean.FALSE.equals(this.schema.getSingleRootElement()) || this.paginator.getTotalResults() == 0L;
    }

    private void expandParents(VocabularyRecord record) {
        if (record.getParentId() != null) {
            findLoadedRecord(record.getParentId()).ifPresent(p -> {
//                p.setShown(true);
//                p.setExpanded(true);
//                expandParents(p);
            });
        }
    }

    private Optional<ExtendedVocabularyRecord> findLoadedRecord(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return paginator.getItems().stream()
                .filter(r -> Objects.equals(r.getId(), id))
                .findFirst();
    }

    private ExtendedVocabularyRecord loadChild(long childId) {
        ExtendedVocabularyRecord newChild = new ExtendedVocabularyRecord(api.vocabularyRecords().get(childId));
        paginator.postLoad(newChild);
        return newChild;
    }

    public void collapseRecord(VocabularyRecord record) {
        paginator.getItems().stream()
                .filter(c -> record.getChildren().contains(c.getId()))
                .forEach(c -> {
//                    c.setShown(false);
                    if (c.getChildren() != null) {
                        collapseRecord(c);
                    }
                });
//        record.setExpanded(false);
    }

    private void loadPaginator() {
        // TODO: Unclean to have static Helper access to user here..
        this.paginator = new HATEOASPaginator<>(
                VocabularyRecordPageResult.class,
                api.vocabularyRecords().list(
                        this.vocabulary.getId(),
                        Optional.of(Helper.getLoginBean().getMyBenutzer().getTabellengroesse()),
                        Optional.empty()
                ),
                () -> comparator.clear(),
                ExtendedVocabularyRecord::new,
                comparator
        );
//        this.paginator.getItems().sort(new JSFVocabularyRecordComparator());
    }

    private void loadSchema() {
        this.schema = api.vocabularySchemas().get(this.vocabulary.getSchemaId());
        this.titleFields = this.schema.getDefinitions().stream()
                .filter(d -> Boolean.TRUE.equals(d.getTitleField()))
                .sorted(Comparator.comparing(FieldDefinition::getId))
                .collect(Collectors.toList());
    }

    private void loadFirstRecord() {
        // TODO: Fix if empty
        if (!this.paginator.getItems().isEmpty()) {
            edit(this.paginator.getItems().get(0));
        } else {
            createEmpty(null);
        }
    }
}
