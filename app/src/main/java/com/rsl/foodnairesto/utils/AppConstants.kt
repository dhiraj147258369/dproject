package com.rsl.foodnairesto.utils

object AppConstants {

    //shared preferences - main login
    const val MY_PREFERENCES = "MY_PREFERENCES"
    const val IS_LOGGED_IN = "IS_LOGGED_IN"
    const val RESTAURANT_NAME = "RESTAURANT_NAME"
    const val RESTAURANT_ID = "RESTAURANT_ID"
    const val RESTAURANT_USER_NAME = "RESTAURANT_USER_NAME"
    const val RESTAURANT_PASSWORD = "RESTAURANT_PASSWORD"
    const val RESTAURANT_LOGO = "RESTAURANT_LOGO"

    const val BUSSINESS_NAME = "BUSSINESS_NAME"

    //shared preferences - settings
    const val AUTO_LOGOUT_ENABLED = "AUTO_LOGOUT_ENABLED"

    //shared preferences - location
    const val SELECTED_LOCATION_NAME = "SELECTED_LOCATION_NAME"
    const val SELECTED_LOCATION_ID = "SELECTED_LOCATION_ID"

    const val LOGGED_IN_SERVER_ID = "LOGGED_IN_SERVER_ID"
    const val LOGGED_IN_SERVER_NAME = "LOGGED_IN_SERVER_NAME"

    const val LOCATION_SERVICE_TYPE = "LOCATION_SERVICE_TYPE"

    const val LOCATION_IS_MANAGER = "LOCATION_IS_MANAGER"

    //shared preferences - tables
    const val SELECTED_TABLE_NO = "SELECTED_TABLE_NO"
    const val SELECTED_TABLE_ID = "SELECTED_TABLE_ID"

    //shared preferences - app settings
    const val SELECTED_BILL_PRINTER_NAME = "selected_bill_printer_name"
    const val SELECTED_BILL_PRINT_PAPER_SIZE = "selected_bill_paper_size"
    const val SELECTED_BILL_PRINTER_NETWORK_IP = "SELECTED_BILL_PRINTER_NETWORK_IP"
    const val SELECTED_BILL_PRINTER_NETWORK_PORT = "SELECTED_BILL_PRINTER_NETWORK_PORT"
    const val SELECTED_BILL_PRINTER_TYPE = "SELECTED_BILL_PRINTER_TYPE"

    const val SELECTED_KITCHEN_PRINTER_NAME = "SELECTED_KITCHEN_PRINTER_NAME"
    const val SELECTED_KITCHEN_PRINT_PAPER_SIZE = "SELECTED_KITCHEN_PRINT_PAPER_SIZE"
    const val SELECTED_KITCHEN_PRINTER_NETWORK_IP = "SELECTED_KITCHEN_PRINTER_NETWORK_IP"
    const val SELECTED_KITCHEN_PRINTER_NETWORK_PORT = "SELECTED_KITCHEN_PRINTER_NETWORK_PORT"
    const val SELECTED_KITCHEN_PRINTER_TYPE = "SELECTED_KITCHEN_PRINTER_TYPE"

    const val QUICK_SERVICE_FRAGMENT_TAB_SELECTED = "QUICK_SERVICE_FRAGMENT_TAB_SELECTED"

    //intents
    const val INTENT_FROM = "INTENT_FROM"

    //arguments
    const val GROUP_ID = "GROUP_ID"
    const val GROUP_NAME = "GROUP_NAME"
    const val SELECTED_GROUP_NAME = "SELECTED_GROUP_NAME"
    const val CATEGORY_ID = "CATEGORY_ID"
    const val PRODUCT_ID = "PRODUCT_ID"

    const val DIALOG_ID = "DIALOG_ID"
    const val DIALOG_SOURCE = "DIALOG_SOURCE"
    const val DIALOG_IMAGE = "DIALOG_IMAGE"
    const val DIALOG_TITLE = "DIALOG_TITLE"
    const val DIALOG_MESSAGE = "DIALOG_MESSAGE"
    const val DIALOG_POSITIVE_BUTTON = "DIALOG_POSITIVE_BUTTON"
    const val DIALOG_NEGATIVE_BUTTON = "DIALOG_NEGATIVE_BUTTON"
    const val DIALOG_POSITIVE_IMAGE = "DIALOG_POSITIVE_IMAGE"
    const val DIALOG_NEGATIVE_IMAGE = "DIALOG_NEGATIVE_IMAGE"

    const val LOADER_TITLE_TEXT = "LOADER_TITLE_TEXT"
    const val LOADER_TEXT = "LOADER_TEXT"



    //dialog related
    const val DIALOG_TYPE = "DIALOG_TYPE"
    const val DIALOG_TYPE_NETWORK = 0
    const val DIALOG_TYPE_OTHER = 1




    //MainProductType
    const val GROUP = 1
    const val CATEGORY = 2
    const val PRODUCT = 3

    //intent constants
    const val FROM_RESTAURANT_LOGIN = 1
    const val FROM_MAIN_SCREEN = 2

    //Date Format
    const val DATE_FORMAT_DMY_HMS = "dd/MM/yyyy HH:mm:ss"

    //Volley /API Basic Constants
    const val AUTH_BASIC = "Basic "
    const val AUTH_CONTENT_TYPE = "Content-Type"
    const val AUTH_CONTENT_TYPE_VALUE = "application/json"

    //API related
    const val API_RESTAURANT_ID = "restaurant_id"

    const val API_USER_ID = "user_id"
    const val API_DATE = "date"

    const val API_TIME = "time"

    const val API_LOCATION_ID = "location_id"
    const val API_PRODUCT_LIST = "product_list"
    const val API_PRODUCT_ID = "product_id"

    const val API_SEQUENCE_NO = "seq_no"

    const val API_OCCUPY_TABLE_ID = "occupy_table_id"
    const val API_OCCUPIED_CHAIRS = "occupiedchairs"
    const val API_TABLE_ID = "table_id"
    const val API_AMOUNT = "amount"
    const val API_GROUP_NAME = "group_name"
    const val API_SEATS = "seats"
    const val API_CART_ID = "cart_id"
    const val API_CART_NO = "cart_no"
    const val API_PRODUCTS = "products"

    const val API_ORDER_TYPE = "order_type"


    const val API_CART_PRODUCT_ID = "cart_product_id"
    const val API_PRODUCT_GROUP_ID = "group_id"
    const val API_PRODUCT_CATEGORY_ID = "category_id"
    const val API_COURSE_TYPE = "course_type"
    const val API_PRODUCT_NAME = "product_name"
    const val API_PRODUCT_QTY = "product_qty"
    const val API_PRODUCT_TYPE = "product_type"
    const val API_PRODUCT_UNIT_PRICE = "product_unit_price"
    const val API_PRODUCT_SPECIAL_INSTRUCTION = "special_instruction"
    const val API_PRODUCT_SPECIAL_INSTRUCTION_PRICE = "special_instruction_price"

    const val API_PRODUCT_INGREDIENTS = "ingredients"
    const val API_CART_INGREDIENTS_ID = "cart_ingredient_id"
    const val API_INGREDIENT_ID = "ingredient_id"
    const val API_INGREDIENT_NAME = "ingredient_name"
    const val API_INGREDIENT_QUANTITY = "ingredient_quantity"
    const val API_INGREDIENT_UNIT_PRICE = "ingredient_unit_price"

    const val API_SUB_PRODUCTS = "sub_products"
    const val API_CART_SUB_PRODUCT_ID = "cart_sub_product_id"
    const val API_SUB_PRODUCT_ID = "sub_product_id"
    const val API_SUB_PRODUCT_CATEGORY_ID = "sub_product_category_id"
    const val API_SUB_PRODUCT_GROUP_ID = "sub_product_group_id"
    const val API_SUB_PRODUCT_NAME = "sub_product_name"
    const val API_SUB_PRODUCT_QUANTITY = "sub_product_qty"
    const val API_SUB_PRODUCT_UNIT_PRICE = "sub_product_unit_price"

    const val API_PRODUCT_ASSIGNED_SEATS = "assigned_seats"
    const val API_PRODUCT_TOTAL_PRICE = "product_total_price"
    const val API_PRODUCT_SEND_TO_KITCHEN_FLAG = "send_to_kitchen_flag"
    const val API_PRODUCT_PRINTER_ID = "printer_id"

    //service type constants
    const val SERVICE_DINE_IN = 1
    const val SERVICE_QUICK_SERVICE = 2

    //quick service ids
    const val QUICK_SERVICE_TABLE_ID = "QUICK_SERVICE_TABLE_ID"
    const val QUICK_SERVICE_CART_ID = "QUICK_SERVICE_CART_ID"
    const val QUICK_SERVICE_CART_NO = "QUICK_SERVICE_CART_NO"
    const val QUICK_SERVICE_TABLE_NO = "QUICK_SERVICE_TABLE_NO"

    //printer related
    const val NO_TYPE = "NO_TYPE"
    const val PAPER_SIZE_50 = 50
    const val PAPER_SIZE_80 = 80
    const val SELECTED_KITCHEN_PRINTER_ID = "SELECTED_KITCHEN_PRINTER_ID"


    //Printer Types
    const val BLUETOOTH_PRINTER = 1
    const val NETWORK_PRINTER = 2
    const val BILL_PRINTER_OR_KITCHEN_PRINTER = "BILL_PRINTER_OR_KITCHEN_PRINTER"
    const val BILL_PRINTER = "BILL_PRINTER"
    const val KITCHEN_PRINTER = "KITCHEN_PRINTER"
    const val BILL_PRINTER_ENABLED = "BILL_PRINTER_ENABLED"


    //modifiers
    const val MULTIPLE_SELECTION_COUNT = "MULTIPLE_SELECTION_COUNT"

    const val SINGLE_SELECTION = 1
    const val MULTIPLE_SELECTION = 2

    //Fragment TAG
    const val CUSTOM_DIALOG_FRAGMENT = "CUSTOM_DIALOG_FRAGMENT"
    const val CUSTOM_PROGRESS_DIALOG_FRAGMENT = "CUSTOM_PROGRESS_DIALOG_FRAGMENT"
}