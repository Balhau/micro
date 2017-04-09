package net.balhau.benchmark.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.balhau.benchmark.avro.warrior.Warrior;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import net.balhau.benchmark.proto.warrior.WarriorOuterClass;

/**
 * Micro benchmark for serializers framework
 */
public class SerializersBenchmark {

    @State(Scope.Thread)
    public static class WarriorAvro{
        public Warrior warrior;

        @Setup()
        public void doSetup(){
            warrior=new Warrior();
            warrior.setAge(134);
            warrior.setName("Avro Wazu");
            warrior.setSkills(Arrays.asList("Sexyness","Boldeness","Flamboyantness"));
        }

        @TearDown()
        public void doTearDown(){
            warrior.setSkills(Arrays.asList(""));
        }
    }

    @State(Scope.Thread)
    public static class WarriorGson{
        public String name;
        public int age;
        public List<String> skills;

        @Setup()
        public void doSetup(){
            System.out.println("Setup warrior");
            name="Gson Wazu";
            age=144;
            skills = Arrays.asList("Swordmanship","Speed","Strongness");
        }

        @TearDown
        public void tearDown(){
            System.out.println("Clean warrior");
        }
    }

    @State(Scope.Thread)
    public static class WarriorProto{
        public WarriorOuterClass.Warrior warrior;

        @Setup()
        public void doSetup(){
            warrior=WarriorOuterClass.Warrior
                    .newBuilder()
                    .setName("Proto Wazu")
                    .setAge(135)
                    .addSkills(WarriorOuterClass.Warrior.Skills.newBuilder().setSkill("Craftman"))
                    .addSkills(WarriorOuterClass.Warrior.Skills.newBuilder().setSkill("Wizardry"))
                    .addSkills(WarriorOuterClass.Warrior.Skills.newBuilder().setSkill("Tunderbolt"))
                    .build();

        }
    }

    public static Gson gson = new GsonBuilder().create();


    @Benchmark
    public String testSerializationGson(WarriorGson warrior){
        return gson.toJson(warrior,WarriorGson.class);
    }

    @Benchmark
    public WarriorGson testSerializationDeserializationGson(WarriorGson warrior){
        return gson.fromJson(gson.toJson(warrior,WarriorGson.class),WarriorGson.class);
    }

    @Benchmark
    public byte[] testSerializationProto(WarriorProto warrior){
        return warrior.warrior.toByteArray();
    }

    @Benchmark
    public WarriorOuterClass.Warrior testSerializationDeserializationProto(WarriorProto warrior) throws Exception{
        return WarriorOuterClass.Warrior.parseFrom(warrior.warrior.toByteArray());
    }

    @Benchmark
    public byte[] testSerializationAvro(WarriorAvro warrior) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        DatumWriter<Warrior> writer = new SpecificDatumWriter<>(Warrior.getClassSchema());

        writer.write(warrior.warrior, encoder);
        encoder.flush();
        out.close();
        return out.toByteArray();
    }

    @Benchmark
    public Warrior testSerializationDeserializationAvro(WarriorAvro warrior) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        DatumWriter<Warrior> writer = new SpecificDatumWriter<>(Warrior.getClassSchema());

        writer.write(warrior.warrior, encoder);
        encoder.flush();
        out.close();

        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(out.toByteArray(),null);
        SpecificDatumReader<Warrior> reader = new SpecificDatumReader<Warrior> (Warrior.getClassSchema());
        return reader.read(null,decoder);
    }
}
