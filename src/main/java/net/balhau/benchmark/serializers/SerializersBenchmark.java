package net.balhau.benchmark.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import net.balhau.benchmark.proto.warrior.WarriorOuterClass;

/**
 * Micro benchmark for serializers framework
 */
public class SerializersBenchmark {

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
    public String testGson(WarriorGson warrior){
        return gson.toJson(warrior,WarriorGson.class);
    }

    @Benchmark
    public byte[] testProto(WarriorProto warrior){
        return warrior.warrior.toByteArray();
    }
}
