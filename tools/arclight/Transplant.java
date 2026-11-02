import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;

/** Transplant one method body (code, try/catch, frames, locals) from a donor class into a base class.
 *  Annotations/signature/access of the base method are kept; aborts if they differ from the donor's. */
public class Transplant {
    static ClassNode read(Path p) throws Exception {
        ClassNode n = new ClassNode();
        new ClassReader(Files.readAllBytes(p)).accept(n, 0);
        return n;
    }
    static MethodNode find(ClassNode c, String name) {
        MethodNode r = null;
        for (MethodNode m : c.methods) if (m.name.equals(name)) { if (r != null) throw new IllegalStateException("ambiguous " + name); r = m; }
        if (r == null) throw new IllegalStateException("missing " + name + " in " + c.name);
        return r;
    }
    static String ann(List<AnnotationNode> l) {
        if (l == null) return "[]";
        StringBuilder sb = new StringBuilder();
        for (AnnotationNode a : l) sb.append(render(a)).append(';');
        return sb.toString();
    }
    static String render(Object v) {
        if (v instanceof AnnotationNode an) {
            StringBuilder sb = new StringBuilder("@").append(an.desc).append('(');
            if (an.values != null) for (int i = 0; i < an.values.size(); i += 2) sb.append(an.values.get(i)).append('=').append(render(an.values.get(i + 1))).append(',');
            return sb.append(')').toString();
        }
        if (v instanceof List<?> li) { StringBuilder sb = new StringBuilder("["); for (Object o : li) sb.append(render(o)).append(','); return sb.append(']').toString(); }
        if (v instanceof String[] arr) return Arrays.toString(arr);
        return String.valueOf(v);
    }
    public static void main(String[] a) throws Exception {
        Path base = Path.of(a[0]), donor = Path.of(a[1]), out = Path.of(a[2]); String name = a[3];
        ClassNode b = read(base), d = read(donor);
        if (!b.name.equals(d.name)) throw new IllegalStateException("class mismatch");
        MethodNode bm = find(b, name), dm = find(d, name);
        if (!bm.desc.equals(dm.desc) || bm.access != dm.access || !Objects.equals(bm.signature, dm.signature))
            throw new IllegalStateException("desc/access/signature mismatch");
        if (!ann(bm.visibleAnnotations).equals(ann(dm.visibleAnnotations)) || !ann(bm.invisibleAnnotations).equals(ann(dm.invisibleAnnotations)))
            throw new IllegalStateException("annotation mismatch:\n" + ann(bm.visibleAnnotations) + "\n" + ann(dm.visibleAnnotations));
        // Reject donor code that touches any Minecraft member (those names differ between Mojmap and SRG).
        for (AbstractInsnNode i : dm.instructions) {
            if (i instanceof MethodInsnNode mi && mi.owner.startsWith("net/minecraft/")) throw new IllegalStateException("MC method ref " + mi.owner + "." + mi.name);
            if (i instanceof FieldInsnNode fi && fi.owner.startsWith("net/minecraft/")) throw new IllegalStateException("MC field ref " + fi.owner + "." + fi.name);
            if (i instanceof InvokeDynamicInsnNode) throw new IllegalStateException("indy not supported");
        }
        bm.instructions = dm.instructions;
        bm.tryCatchBlocks = dm.tryCatchBlocks;
        bm.localVariables = dm.localVariables;
        bm.visibleLocalVariableAnnotations = dm.visibleLocalVariableAnnotations;
        bm.invisibleLocalVariableAnnotations = dm.invisibleLocalVariableAnnotations;
        bm.visibleTypeAnnotations = dm.visibleTypeAnnotations;
        bm.invisibleTypeAnnotations = dm.invisibleTypeAnnotations;
        bm.maxStack = dm.maxStack;
        bm.maxLocals = dm.maxLocals;
        ClassWriter cw = new ClassWriter(0); // keep donor's javac-emitted stack map frames verbatim
        b.accept(cw);
        Files.write(out, cw.toByteArray());
        System.out.println("transplanted " + b.name + "." + name + dm.desc + " (" + dm.instructions.size() + " insns, " + dm.tryCatchBlocks.size() + " handlers)");
    }
}
