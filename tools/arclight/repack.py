"""Rebuild a zip/jar, replacing selected entries' bytes, preserving order, compression and metadata of all others."""
import sys, zipfile, time
def repack(src, dst, replacements):
    seen = set()
    with zipfile.ZipFile(src) as zi, zipfile.ZipFile(dst, 'w') as zo:
        for info in zi.infolist():
            data = zi.read(info.filename)
            if info.filename in replacements:
                data = replacements[info.filename]; seen.add(info.filename)
                ni = zipfile.ZipInfo(info.filename, date_time=time.localtime()[:6])
                ni.compress_type = info.compress_type; ni.external_attr = info.external_attr
                zo.writestr(ni, data)
            else:
                zo.writestr(info, data)
    missing = set(replacements) - seen
    if missing: raise SystemExit(f"entries not found: {missing}")
if __name__ == '__main__':
    mode = sys.argv[1]
    if mode == 'common':   # src dst classfile entryname
        repack(sys.argv[2], sys.argv[3], {sys.argv[5]: open(sys.argv[4], 'rb').read()})
    elif mode == 'outer':  # src dst common.jar new-version
        src, dst, common, ver = sys.argv[2:6]
        with zipfile.ZipFile(src) as z: mf = z.read('META-INF/MANIFEST.MF').decode()
        lines = []
        for l in mf.split('\r\n') if '\r\n' in mf else mf.split('\n'):
            if l.startswith('Implementation-Version:'): l = 'Implementation-Version: ' + ver
            elif l.startswith('Implementation-Timestamp:'): l = 'Implementation-Timestamp: ' + time.strftime('%Y-%m-%d %H:%M:%S')
            lines.append(l)
        nl = '\r\n' if '\r\n' in mf else '\n'
        repack(src, dst, {'common.jar': open(common, 'rb').read(), 'META-INF/MANIFEST.MF': nl.join(lines).encode()})
