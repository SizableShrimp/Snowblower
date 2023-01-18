/*
 * Snowblower
 * Copyright (C) 2023 Forge Development LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package net.minecraftforge.snowblower;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cache {
    private final Map<String, String> data = new LinkedHashMap<>();
    private String comment = null;

    public Cache comment(String... lines) {
        if (lines == null || lines.length == 0)
            comment = null;
        else
            comment = Stream.of(lines).collect(Collectors.joining("\n"));
        return this;
    }

    public Cache put(String key, String value) {
        data.put(key, value);
        return this;
    }

    public Cache put(String key, Path path) throws IOException {
        data.put(key, HashFunction.SHA1.hash(path));
        return this;
    }

    public void write(Path target) throws IOException {
        StringBuilder buf = new StringBuilder();
        if (comment != null)
            buf.append(comment).append("\n\n");
        data.forEach((k,v) -> buf.append(k).append(": ").append(v).append('\n'));
        Files.write(target, buf.toString().getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(Path target) throws IOException {
        if (!Files.exists(target))
            return false;

        Map<String, String> existing = new HashMap<>();
        try (Stream<String> stream = Files.lines(target)) {
            stream.forEach(l -> {
                int idx = l.indexOf(' ');
                if (idx <= 1 || l.charAt(idx - 1) != ':') // We don't care about comments.
                    return;

                String key = l.substring(0, idx - 1);
                String value = l.substring(idx + 1);
                existing.put(key, value);
            });
        }
        return existing.equals(data);
    }

}