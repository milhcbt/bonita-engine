/*
 * * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.document.model.builder.impl;

import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SDocumentBuilderFactoryImpl implements SDocumentBuilderFactory {

    static final String ID = "id";

    static final String NAME = "name";

    static final String AUTHOR = "author";

    static final String CREATION_DATE = "creationDate";

    static final String HAS_CONTENT = "hasContent";

    static final String FILENAME = "fileName";

    static final String MIMETYPE = "mimeType";

    static final String URL = "url";

    static final String VERSION = "version";

    static final String DESCRIPTION = "description";

    static final String INDEX = "index";

    @Override
    public SDocumentBuilder createNewInstance(final String fileName, final String mimetype, final long authorId) {
        final SDocumentBuilderImpl sDocumentBuilderImpl = new SDocumentBuilderImpl();
        sDocumentBuilderImpl.setFileName(fileName);
        sDocumentBuilderImpl.setMimeType(mimetype);
        sDocumentBuilderImpl.setAuthor(authorId);
        sDocumentBuilderImpl.setCreationDate(System.currentTimeMillis());
        return sDocumentBuilderImpl;
    }

    @Override
    public SDocumentBuilder createNewProcessDocument(final String fileName, final String mimetype, final long authorId, final byte[] content) {
        final SDocumentBuilder sDocumentBuilder = createNewInstance(fileName, mimetype, authorId);
        sDocumentBuilder.setContent(content);
        sDocumentBuilder.setHasContent(true);
        return sDocumentBuilder;
    }

    @Override
    public SDocumentBuilder createNewExternalProcessDocumentReference(final String fileName, final String mimetype, final long authorId, final String url) {
        final SDocumentBuilder sDocumentBuilder = createNewInstance(fileName, mimetype, authorId);
        sDocumentBuilder.setURL(url);
        sDocumentBuilder.setHasContent(false);
        return sDocumentBuilder;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getNameKey() {
        return NAME;
    }

    @Override
    public String getAuthorKey() {
        return AUTHOR;
    }

    @Override
    public String getCreationDateKey() {
        return CREATION_DATE;
    }

    @Override
    public String getHasContentKey() {
        return HAS_CONTENT;
    }

    @Override
    public String getFileNameKey() {
        return FILENAME;
    }

    @Override
    public String getMimeTypeKey() {
        return MIMETYPE;
    }

    @Override
    public String getURLKey() {
        return URL;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

    @Override
    public String getVersionKey() {
        return VERSION;
    }

    @Override
    public String getIndexKey() {
        return INDEX;
    }

}
