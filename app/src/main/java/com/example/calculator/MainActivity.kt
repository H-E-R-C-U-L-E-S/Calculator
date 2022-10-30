package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.View
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView

    //მოქმედებების შესრულების შესაძლებლობა
    private var opAllow = false

    //წერტილის დაწერის შესაძლებლობა
    private var decimalAllow = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ინიტიალიზაცია
        resultTextView = findViewById(R.id.resultTextView)

    }

    fun onClickNumber(view: View) {
        //ვამოწმებთ არის თუ არა ტექსტვიუ
        if (view is TextView) {
            //თუ წერტილს დაკლიკეს
            if (view.text == ".") {
                //ვამოწმებთ წერტილი დაწერილი ხომ არ არის
                if (decimalAllow) {
                    //თუ ოპერატორის შემდეგ პირდაპირ წერტილს ვწერთ ჩავამატოთ ნული წერტილის წინ
                    if (!resultTextView.text.last().isDigit()){
                        resultTextView.append("0")
                    }
                    //თუ არ არის ვწერთ წერტილს და ვაახლებთ ბულიენის მნიშვნელობას
                    resultTextView.append(view.text)
                    decimalAllow = false
                }

            } else {
                //თუ ტექსტვიუ მხოლოდ 0-ს აჩვენებს წავშალოთ (ახალი ციფრის დამატებისას საწყისი 0 რომ გაქრეს)
                if (resultTextView.text.toString() == "0") {
                    resultTextView.text = ""
                }
                //ტექსტვიუში ვამატებთ ახალ ციფრს
                resultTextView.append(view.text)
                // ბულიენს ვცვლით რაც სშუალებას გვაძლევს მოქმედებები შევასრულოთ(+,-,*,/)
                opAllow = true
            }
        }
    }

    fun onClickOperator(view: View) {
        //ვამოწმებთ არის თუ არა ტექსტვიუ
        if (view is TextView) {
            //თუ უარყოფითი რიცხვით გვინდა დაწყება ან წერტილის შემდეგ პირდაპირ ვწერთ ოპერატორს
            if (resultTextView.text.toString() == "0" && view.text.equals("-") || resultTextView.text.last().toString() == ".") {
                resultTextView.text = resultTextView.text.dropLast(1)
                resultTextView.append((view.text))
                opAllow = false
            }
            //ვამოწმებთ შეგვიძლია თუ არა მოქმედებების შესრულება
            else if (opAllow) {
                //თუ შეგვიძლია მაშინ ოპერატორს ვუთითებთ ტექსტვიუში და ბულიენებს ვაახლებთ
                resultTextView.append(view.text)
                opAllow = false
                decimalAllow = true
            } else {
                if (resultTextView.text.length > 1) {
                    //თუ ოპერატორი უკვე წერია ვშლით და ვუთითებთ ახალ ოპერატორს
                    resultTextView.text = resultTextView.text.dropLast(1)
                    resultTextView.append((view.text))
                }
            }
        }
    }

    fun onClickClear(view: View) {
        //ვამოწმებთ არის თუ არა ტექსტვიუ
        if (view is TextView) {
            //ვაყენებთ საწყის მნიშვნელობებზე
            resultTextView.text = "0"
            opAllow = true
            decimalAllow = true
        }
    }

    fun onClickDelete(view: View) {
        //ვამოწმებთ არის თუ არა ტექსტვიუ
        if (view is TextView) {
            //თუ 1 ციფრიღაა დარჩენილი ვუშვებთ გასუფთავების ფუნქციას
            if (resultTextView.text.length == 1) {
                onClickClear(view)
            } else {
                //თუ არადა ბოლო ციფრს ან ოპერატორს ვშლით
                if (!resultTextView.text.last().isDigit()) {
                    opAllow = true
                }
                resultTextView.text = resultTextView.text.dropLast(1)
            }
        }
    }

    fun onClickEquals(view: View) {
        //ვამოწმებთ არის თუ არა ტექსტვიუ
        if (view is TextView) {
            //ვუშვებთ calculateResult ფუნქციას ვთვლით საბოლოო მნიშვნელობას და ვაყენებთ ტექსტვიუზე
            if (resultTextView.text.toString().length > 1) {
                resultTextView.text = calculateResults()
            }
        }
    }

    private fun calculateResults(): String {
        //ვარკვევთ რომელი რიცხვებს შორის რომელი ოპერატორებია და ვამატებთ მუთეიბლ სიაში ფუნქციით numOperators()
        val numOperators = numOperators()
        //ვამოწმებთ სია ცარიელი ხომ არ არის
        if (numOperators.isEmpty()) return ""

        //თუ სია ცარიელი არ არის ჯერ ვასრულებთ გამრავლებას და გაყოფას ფუნქციით timesDivisionCalculate()
        val timesDivision = timesDivisionCalculate(numOperators)

        //შემდეგ ვასრულებთ მიმატებას და გამოკლებას
        val result = addSubtractCalculate(timesDivision)
        //double გადავაქციოთ ბიგდეციმალად და  ზედმეტი 0 ების მოსაშორებლად ვუხმოთ ფუნქციას stripTrailingZeros
        //შემდეგ კი აუცილებლად  toPlainString ფუნქციით გადავაქციოთ სთრინგად და არა toString -ით
        return result.toBigDecimal().stripTrailingZeros().toPlainString()
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Double {
        //ჩაწოდებული სიის პირველი ელემენტი
        var result = passedList[0] as Double
        //ფუნქცია indices აბრუნებს ინტ რეინჯს სიისთვის
        for (i in passedList.indices) {
            //ვამოწმებთ არის თუ არა სიის i ელემნტი ოპერატორი
            //ვამოწმებთ რომ i არ უდრის სიის ბოლო ელემენტის ინდქსს
            if (passedList[i] is Char && i != passedList.lastIndex) {
                //ოპერატორი
                val operator = passedList[i]
                //შემდეგი რიცხვი
                val nextNum = passedList[i + 1] as Double
                //ვასრულებთ შესაბამის მოქმედებებს
                if (operator == '+') result += nextNum
                if (operator == '-') result -= nextNum
            }
        }

        return result
    }

    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        //ჩაწოდებული სია
        var list = passedList
        //ვიდრე ჩაწოდებული სია შეიცავს გამრავლების და გაყოფის ოპერატორებს ის ასრულებს ქმედებებს
        while (list.contains('*') || list.contains('/')) {
            list = calcTimesDiv(list)
        }
        //აბრუნებს სიას
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        //ვქმნით ახალ სიას
        val newList = mutableListOf<Any>()
        //ვიღებთ ინდექსს რესტარტისთვის რომელიც ტოლია ჩაწოდებული სიის ზომის
        var restartIndex = passedList.size

        //ფუნქცია indices აბრუნებს ინტ რეინჯს სიისთვის
        for (i in passedList.indices) {
            //passedList[i] is Char ამოწმებს სიის კონრეტული ელემენტი არის თუ არა ჩარაქთერი რათა რიცხვის შემთხვევაში არ გაეშვას ფუნქცია
            //i != passedList.lastIndex ამოწმებს i - ის მნიშვნელობა სიის ბოლო ინდექსს ხომ არ ემთხვევა რათა არ გავცდეთ სიას
            //i < restartIndex ამოწმებს რომ i არ გასცდეს რესტარტის ინდექსს
            if (passedList[i] is Char && i != passedList.lastIndex && i < restartIndex) {

                //ოპერატორის ტიპი
                val operator = passedList[i]
                //ვიგებთ წინა და მომდევნო რიცხვს
                val prevDigit = passedList[i - 1] as Double
                val nextDigit = passedList[i + 1] as Double

                //ვამოწმებთ ოპერატორის ტიპს
                when (operator) {
                    '*' -> {
                        //ვასრულებთ მოქმედებას და ინდექსს ვწევთ ერთით წინ
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i + 1
                    }
                    '/' -> {
                        //ვასრულებთ მოქმედებას და ინდექსს ვწევთ ერთით წინ
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }
                    else -> {
                        //თუ ოპერატორი არის - ან + წინა რიცხვს და ოპერატორს ვამატებთ სიაში
                        //მომდევნო რიცხვს იმიტომ არ ვამატებთ რომ როცა ინდექსი გადაიწევს 1-ით და ფუნქცია გაეშვება რიცხვი სიაში 2ჯერ არ მოხვდეს
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }
            //თუ i-ის მნიშვნელობა გასცდება რესტარტ ინდექსს ახალ სიაში ვამატებთ ჩაწოდებული სიის i -ზე მდგომ ელემენტს
            if (i > restartIndex) {
                newList.add(passedList[i])
            }
        }

        //ვაბრუნებთ ახალ სიას
        return newList
    }

    private fun numOperators(): MutableList<Any> {
        //ვქმნით მუთეიბლ სიას "Any" პარამეტრით რომელსაც შეგვიძლია დავამატოთ და ამოვაკლოთ ნებისმიერი სახის  ობიექტი
        val list = mutableListOf<Any>()
        //მიმდინარე ციფრი
        var currentNum = ""

        //თვლა
        var i = 0

        //თექსთვიუს ყველა ჩარაქთერისთვის სათითაოდ გაეშვება ფუნქცია
        for (character in resultTextView.text) {
            //თუ ჩარაქთერი ან რიცხვია ან წერტილი აღიქმება ერთ რიცხვად სანამ არ მივალთ ოპერატორამდე
            if (character.isDigit() || character == '.') currentNum += character
            else {
                //თუ მინუსით იწყება რიცხვი ოპერატორის ნაცვალად აღიქვამს რიცხვის ნიშნად
                //ვამოწმებთ პირველი ჩარაქთერი არის თუ არა -
                if (i == 0 && character.toString() == "-") {
                    currentNum += character
                } else {
                    //სიაში ვამატებთ "შეკრულ" რიცხვს როგორც double
                    list.add(currentNum.toDouble())
                    //მიმდინარე რიცხვს ვაბრუნბთ საწყის მნიშვნელობაზე
                    currentNum = ""
                    //სიაში ვამატებთ ოპერატორს
                    list.add(character)
                }
            }
            i++
        }

        //როდესაც for ლუპი ბოლოში გავა მასში ჩაწერილი ფუნქცია მეტჯერ აღარ გაეშვება რადგან  მეტი ჩარაქთერი აღარაა და შესაბამისად ოპერატორამდეც ვერ მივა
        // შესაბამისად ბოლო რიცხვი რომელსაც შეადგენს არ დაემატება სიაში რადგან ლოგიკა აწყობილია ოპერატორამდე მისვლაზე
        //ამიტომ ლუპის გარეთ უნდა დავამატოთ ბოლო "შეკრული" რიცხვი
        if (currentNum != "") {
            list.add(currentNum.toDouble())
        }

        //ვაბრუნებთ სიას რომელიც შეცავს "შეკრულ" რიცხვებს და ოპერატორებს
        //ამრიგად ჩვენ უკვე ვიცით რომელი რიცხვებს შორის რომელი ოპერატორია
        return list
    }

}














